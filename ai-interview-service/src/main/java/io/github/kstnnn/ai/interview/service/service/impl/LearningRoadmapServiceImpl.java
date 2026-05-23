package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.dto.LearningResourceDto;
import io.github.kstnnn.ai.interview.service.dto.LearningRoadmapDto;
import io.github.kstnnn.ai.interview.service.dto.LearningRoadmapTopicDto;
import io.github.kstnnn.ai.interview.service.dto.UserLearningRoadmapDto;
import io.github.kstnnn.ai.interview.service.dto.UserLearningRoadmapTopicDto;
import io.github.kstnnn.ai.interview.service.model.InterviewSession;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionStatus;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionType;
import io.github.kstnnn.ai.interview.service.model.LearningResource;
import io.github.kstnnn.ai.interview.service.repository.InterviewSessionRepository;
import io.github.kstnnn.ai.interview.service.repository.LearningResourceRepository;
import io.github.kstnnn.ai.interview.service.service.LearningRoadmapService;
import io.github.kstnnn.ai.interview.service.service.TopicStateService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LearningRoadmapServiceImpl implements LearningRoadmapService {

  private final InterviewSessionRepository interviewSessionRepository;
  private final TopicStateService topicStateService;
  private final LearningResourceRepository learningResourceRepository;

  @Override
  @Transactional(readOnly = true)
  public LearningRoadmapDto getRoadmap(UUID sessionId, UUID userId, String language) {
    var session = interviewSessionRepository.findById(sessionId).orElseThrow();
    if (!session.getUserId().equals(userId)) {
      throw new IllegalArgumentException("Interview session does not belong to current user");
    }
    if (session.getSessionType() != InterviewSessionType.MOCK) {
      throw new IllegalStateException("Learning roadmap is available only for mock interviews");
    }
    if (session.getStatus() != InterviewSessionStatus.COMPLETED) {
      throw new IllegalStateException("Learning roadmap is available only after interview completion");
    }

    var lang = normalizeLanguage(language);
    var topics =
        topicStateService.getTopicSummaries(sessionId).stream()
            .sorted(Comparator.comparing(topic -> topic.avgScore()))
            .limit(5)
            .map(topic -> toTopic(lang, topic.topic(), topic.avgScore().doubleValue()))
            .toList();
    return new LearningRoadmapDto(sessionId, lang, summary(lang, topics), topics);
  }

  @Override
  @Transactional(readOnly = true)
  public UserLearningRoadmapDto getUserRoadmap(UUID userId, String language) {
    var lang = normalizeLanguage(language);
    var sessions =
        interviewSessionRepository
            .findTop10ByUserIdAndSessionTypeAndStatusOrderByFinishedAtDesc(
                userId, InterviewSessionType.MOCK, InterviewSessionStatus.COMPLETED);
    var sourceSessionIds = sessions.stream().map(InterviewSession::getId).toList();
    if (sessions.isEmpty()) {
      return new UserLearningRoadmapDto(
          userId, lang, Instant.now(), List.of(), emptyUserSummary(lang), List.of());
    }

    var currentCount = Math.max(1, Math.min(5, (sessions.size() + 1) / 2));
    var currentScores = aggregateTopicScores(sessions.subList(0, currentCount));
    var previousScores =
        currentCount < sessions.size()
            ? aggregateTopicScores(sessions.subList(currentCount, sessions.size()))
            : Map.<String, TopicScore>of();

    var topics =
        currentScores.entrySet().stream()
            .map(entry -> toUserTopic(lang, entry.getValue(), previousScores.get(entry.getKey())))
            .sorted(
                Comparator.comparing(UserLearningRoadmapTopicDto::priority, this::comparePriority)
                    .thenComparing(UserLearningRoadmapTopicDto::currentScore))
            .limit(8)
            .toList();

    return new UserLearningRoadmapDto(
        userId,
        lang,
        sessions.get(0).getFinishedAt() != null ? sessions.get(0).getFinishedAt() : Instant.now(),
        sourceSessionIds,
        userSummary(lang, topics),
        topics);
  }

  private LearningRoadmapTopicDto toTopic(String language, String topic, double score) {
    return new LearningRoadmapTopicDto(
        topic,
        score,
        reason(language, topic, score),
        actions(language, topic),
        resources(topic, language));
  }

  private UserLearningRoadmapTopicDto toUserTopic(
      String language, TopicScore currentScore, TopicScore previousScore) {
    var current = currentScore.average();
    var previous = previousScore != null ? previousScore.average() : null;
    var trend = trend(current, previous);
    var priority = priority(current);
    return new UserLearningRoadmapTopicDto(
        currentScore.displayTopic,
        current,
        previous,
        trend,
        priority,
        userReason(language, currentScore.displayTopic, current, previous, trend),
        actions(language, currentScore.displayTopic),
        resources(currentScore.displayTopic, language));
  }

  private Map<String, TopicScore> aggregateTopicScores(List<InterviewSession> sessions) {
    var scores = new HashMap<String, TopicScore>();
    for (var session : sessions) {
      for (var topic : topicStateService.getTopicSummaries(session.getId())) {
        var normalizedTopic = normalizeTopic(topic.topic());
        scores.computeIfAbsent(normalizedTopic, ignored -> new TopicScore(topic.topic()))
            .add(topic.avgScore().doubleValue());
      }
    }
    return scores;
  }

  private List<LearningResourceDto> resources(String topic, String language) {
    var normalizedTopic = normalizeTopic(topic);
    return learningResourceRepository.findByActiveTrueOrderByTopicAscTitleAsc().stream()
        .filter(resource -> matchesTopic(resource, normalizedTopic))
        .sorted(Comparator.comparing((LearningResource r) -> languagePriority(r, language)))
        .limit(4)
        .map(r -> new LearningResourceDto(r.getTitle(), r.getUrl(), r.getType().name(), r.getLanguage()))
        .toList();
  }

  private boolean matchesTopic(LearningResource resource, String topic) {
    var resourceTopic = normalizeTopic(resource.getTopic());
    return resourceTopic.equals(topic) || topic.contains(resourceTopic) || resourceTopic.contains(topic);
  }

  private int languagePriority(LearningResource resource, String language) {
    var resourceLanguage = resource.getLanguage().toLowerCase(Locale.ROOT);
    if (resourceLanguage.equals(language)) return 0;
    if (resourceLanguage.equals("any")) return 1;
    if (resourceLanguage.equals("en")) return 2;
    return 3;
  }

  private String summary(String language, List<LearningRoadmapTopicDto> topics) {
    if (language.equals("ru")) {
      return topics.isEmpty()
          ? "Интервью завершено. Продолжай практиковаться, чтобы закрепить знания."
          : "Сфокусируйся на темах с самой низкой оценкой и закрепи их практикой.";
    }
    return topics.isEmpty()
        ? "The interview is complete. Keep practicing to reinforce your knowledge."
        : "Focus on your lowest-scoring topics and reinforce them with practical exercises.";
  }

  private String emptyUserSummary(String language) {
    if (language.equals("ru")) {
      return "Пройди mock-интервью, чтобы получить персональный roadmap.";
    }
    return "Complete a mock interview to get a personalized learning roadmap.";
  }

  private String userSummary(String language, List<UserLearningRoadmapTopicDto> topics) {
    if (topics.isEmpty()) {
      return emptyUserSummary(language);
    }
    var highCount = topics.stream().filter(topic -> topic.priority().equals("HIGH")).count();
    if (language.equals("ru")) {
      return highCount > 0
          ? "Твой roadmap обновлен. Начни с тем с высоким приоритетом."
          : "Твой roadmap обновлен. Продолжай закреплять темы со средним приоритетом.";
    }
    return highCount > 0
        ? "Your roadmap is updated. Start with high-priority topics."
        : "Your roadmap is updated. Keep reinforcing medium-priority topics.";
  }

  private String reason(String language, String topic, double score) {
    var percent = Math.round(score * 100);
    if (language.equals("ru")) {
      return "Тема " + topic + " получила около " + percent + "%, поэтому её стоит повторить.";
    }
    return "Topic " + topic + " scored around " + percent + "%, so it should be reviewed.";
  }

  private String userReason(
      String language, String topic, double currentScore, Double previousScore, String trend) {
    var currentPercent = Math.round(currentScore * 100);
    if (previousScore == null) {
      return language.equals("ru")
          ? "Текущая оценка по теме " + topic + " около " + currentPercent + "%."
          : "Current score for " + topic + " is around " + currentPercent + "%.";
    }
    var previousPercent = Math.round(previousScore * 100);
    return language.equals("ru")
        ? "Тема " + topic + ": сейчас около " + currentPercent + "%, раньше было около " + previousPercent + "%. Тренд: " + trend + "."
        : "Topic " + topic + ": current score is around " + currentPercent + "%, previous score was around " + previousPercent + "%. Trend: " + trend + ".";
  }

  private List<String> actions(String language, String topic) {
    if (language.equals("ru")) {
      return List.of(
          "Повтори ключевые концепции по теме: " + topic,
          "Сделай небольшой практический пример и объясни архитектурные решения",
          "Подготовь 2-3 примера из реального опыта для следующего интервью");
    }
    return List.of(
        "Review the core concepts for: " + topic,
        "Build a small practical example and explain your design decisions",
        "Prepare 2-3 real experience examples for the next interview");
  }

  private String trend(double currentScore, Double previousScore) {
    if (previousScore == null) return "NEW";
    if (currentScore > previousScore + 0.05) return "IMPROVING";
    if (currentScore < previousScore - 0.05) return "DECLINING";
    return "STABLE";
  }

  private String priority(double score) {
    if (score < 0.5) return "HIGH";
    if (score < 0.75) return "MEDIUM";
    return "LOW";
  }

  private int comparePriority(String left, String right) {
    return Integer.compare(priorityRank(left), priorityRank(right));
  }

  private int priorityRank(String priority) {
    return switch (priority) {
      case "HIGH" -> 0;
      case "MEDIUM" -> 1;
      default -> 2;
    };
  }

  private String normalizeLanguage(String language) {
    if (language == null || language.isBlank()) return "en";
    var normalized = language.toLowerCase(Locale.ROOT);
    return normalized.startsWith("ru") ? "ru" : "en";
  }

  private String normalizeTopic(String topic) {
    if (topic == null || topic.isBlank()) return "general";
    return topic.toLowerCase(Locale.ROOT).replaceAll("[^a-zа-я0-9]+", "").trim();
  }

  private static final class TopicScore {
    private final String displayTopic;
    private final List<Double> scores = new ArrayList<>();

    private TopicScore(String displayTopic) {
      this.displayTopic = displayTopic;
    }

    private void add(double score) {
      scores.add(score);
    }

    private double average() {
      return scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
  }
}
