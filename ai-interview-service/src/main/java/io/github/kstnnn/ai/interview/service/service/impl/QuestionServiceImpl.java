package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.model.Difficulty;
import io.github.kstnnn.ai.interview.service.model.Question;
import io.github.kstnnn.ai.interview.service.repository.QuestionRepository;
import io.github.kstnnn.ai.interview.service.service.QuestionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

  private static final int QUESTION_CANDIDATE_POOL_SIZE = 10;

  private final VectorStore vectorStore;
  private final QuestionRepository qRepository;

  @Override
  public List<Question> getBaseQuestions(List<String> technologyKeys) {
    var normalizedKeys =
        technologyKeys == null
            ? List.<String>of()
            : technologyKeys.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

    if (normalizedKeys.isEmpty()) {
      throw new IllegalArgumentException("Technology keys must not be empty");
    }

    log.info("Base questions requested for technologyKeys={}", normalizedKeys);

    List<Question> questions = new ArrayList<>();
    for (String key : normalizedKeys) {
      questions.addAll(getQuestionsPerTechnology(key));
    }
    return questions;
  }

  @Override
  public Optional<Question> findReinforcementQuestion(
      String topic, Difficulty difficulty, List<String> excludeExternalIds) {
    var filterExpressionBuilder = new FilterExpressionBuilder();
    var expression =
        filterExpressionBuilder
            .and(
                filterExpressionBuilder.eq("topic", topic),
                filterExpressionBuilder.eq("difficulty", difficulty.toString()))
            .build();

    var searchRequest =
        SearchRequest.builder()
            .query(topic)
            .similarityThreshold(0.6)
            .topK(5)
            .filterExpression(expression)
            .build();

    var results =
        vectorStore.similaritySearch(searchRequest).stream()
            .map(d -> (String) d.getMetadata().get("id"))
            .filter(id -> !excludeExternalIds.contains(id))
            .toList();

    if (results.isEmpty()) {
      log.info("No reinforcement question found for topic={}, difficulty={}", topic, difficulty);
      return Optional.empty();
    }

    return qRepository.findAllByExternalIdInAndActiveTrue(results).stream().findFirst();
  }

  private List<Question> getQuestionsPerTechnology(String technology) {
    List<Question> questions = new ArrayList<>();
    var filterExpressionBuilder = new FilterExpressionBuilder();

    for (var difficulty : Difficulty.values()) {
      var expression =
          filterExpressionBuilder
              .and(
                  filterExpressionBuilder.eq("technology", technology),
                  filterExpressionBuilder.eq("difficulty", difficulty.toString()))
              .build();

      var questionExternalIds =
          vectorStore
              .similaritySearch(
                  SearchRequest.builder()
                      .similarityThreshold(0.7)
                      .topK(QUESTION_CANDIDATE_POOL_SIZE)
                      .filterExpression(expression)
                      .build())
              .stream()
              .map(d -> (String) d.getMetadata().get("id"))
              .toList();

      pickRandom(qRepository.findAllByExternalIdInAndActiveTrue(questionExternalIds))
          .ifPresent(questions::add);
    }

    return questions;
  }

  private Optional<Question> pickRandom(List<Question> questions) {
    if (questions.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(questions.get(ThreadLocalRandom.current().nextInt(questions.size())));
  }
}
