package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.dto.AskQuestionDto;
import io.github.kstnnn.ai.interview.service.dto.EvaluationResultDto;
import io.github.kstnnn.ai.interview.service.dto.FollowUpQuestionDto;
import io.github.kstnnn.ai.interview.service.dto.NextQuestionResult;
import io.github.kstnnn.ai.interview.service.dto.SubmitAnswerDto;
import io.github.kstnnn.ai.interview.service.model.AnswerEvaluation;
import io.github.kstnnn.ai.interview.service.model.Difficulty;
import io.github.kstnnn.ai.interview.service.model.InterviewFinishedReason;
import io.github.kstnnn.ai.interview.service.model.InterviewSession;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionStatus;
import io.github.kstnnn.ai.interview.service.model.PlannedStatus;
import io.github.kstnnn.ai.interview.service.model.Question;
import io.github.kstnnn.ai.interview.service.model.QuestionType;
import io.github.kstnnn.ai.interview.service.model.SelectionReason;
import io.github.kstnnn.ai.interview.service.model.SessionAnswer;
import io.github.kstnnn.ai.interview.service.model.SessionQuestion;
import io.github.kstnnn.ai.interview.service.repository.AnswerEvaluationRepository;
import io.github.kstnnn.ai.interview.service.repository.InterviewSessionRepository;
import io.github.kstnnn.ai.interview.service.repository.PlannedSessionQuestionRepository;
import io.github.kstnnn.ai.interview.service.repository.SessionAnswerRepository;
import io.github.kstnnn.ai.interview.service.repository.SessionQuestionRepository;
import io.github.kstnnn.ai.interview.service.service.AiService;
import io.github.kstnnn.ai.interview.service.service.InterviewFlowService;
import io.github.kstnnn.ai.interview.service.service.QuestionService;
import io.github.kstnnn.ai.interview.service.service.TopicStateService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewFlowServiceImpl implements InterviewFlowService {

  private final AiService aService;
  private final QuestionService qService;
  private final TopicStateService topicStateService;
  private final InterviewSessionRepository iSessionRepository;
  private final SessionQuestionRepository sQuestionRepository;
  private final SessionAnswerRepository sAnswerRepository;
  private final AnswerEvaluationRepository aEvaluationRepository;
  private final PlannedSessionQuestionRepository pQuestionRepository;

  @Transactional
  @Override
  public void startSession(UUID sessionId, String interviewLanguage) {
    var session = iSessionRepository.findById(sessionId).orElseThrow();
    if (interviewLanguage != null && !interviewLanguage.isBlank()) {
      session.setInterviewLanguage(interviewLanguage);
    }
    if (session.getStatus() == InterviewSessionStatus.IN_PROGRESS) {
      return;
    }
    if (session.getStatus() != InterviewSessionStatus.CREATED) {
      throw new IllegalStateException(
          "Session %s cannot be started, current status: %s"
              .formatted(sessionId, session.getStatus()));
    }
    session.setStatus(InterviewSessionStatus.IN_PROGRESS);
    session.setStartedAt(Instant.now());
  }

  @Transactional
  @Override
  public EvaluationResultDto submitAnswer(UUID sessionId, SubmitAnswerDto dto) {
    var session = loadAndValidateInProgress(sessionId);

    var existingAnswer = sAnswerRepository.findBySessionQuestionId(dto.sessionQuestionId());
    if (existingAnswer.isPresent()) {
      log.warn("Answer already submitted for question {}, returning existing evaluation", dto.sessionQuestionId());
      var eval = aEvaluationRepository.findBySessionAnswerId(existingAnswer.get().getId()).orElseThrow();
      return new EvaluationResultDto(
          eval.getCorrectnessScore().doubleValue(),
          eval.getDepthScore().doubleValue(),
          eval.getPracticalScore().doubleValue(),
          eval.getTotalScore().doubleValue(),
          0.0,
          List.of(),
          List.of(),
          false,
          null,
          eval.getFeedback(),
          true);
    }

    var sessionQuestion = sQuestionRepository.findById(dto.sessionQuestionId()).orElseThrow();

    if (!sessionQuestion.getSession().getId().equals(sessionId)) {
      throw new IllegalArgumentException("Question does not belong to session " + sessionId);
    }

    var answer = new SessionAnswer();
    answer.setSessionQuestion(sessionQuestion);
    answer.setAnswerText(dto.answerText());
    sAnswerRepository.save(answer);

    var evaluation =
        normalizeEvaluation(
            aService.evaluateAnswer(
                sessionQuestion.getQuestionTextSnapshot(),
                sessionQuestion.getQuestion() != null
                    ? sessionQuestion.getQuestion().getExpectedAnswer()
                    : "",
                dto.answerText(),
                resolveInterviewLanguage(session)));

    var evaluationEntity = new AnswerEvaluation();
    evaluationEntity.setSessionAnswer(answer);
    evaluationEntity.setCorrectnessScore(toScoreValue(evaluation.correctnessScore()));
    evaluationEntity.setDepthScore(toScoreValue(evaluation.depthScore()));
    evaluationEntity.setPracticalScore(toScoreValue(evaluation.practicalScore()));
    evaluationEntity.setTotalScore(toScoreValue(evaluation.totalScore()));
    evaluationEntity.setFeedback(evaluation.candidateFeedback());
    evaluationEntity.setKnowledgeGapsJson(serializeKnowledgeGaps(evaluation.knowledgeGaps()));
    aEvaluationRepository.save(evaluationEntity);

    topicStateService.updateAfterAnswer(
        sessionId,
        sessionQuestion.getTopic(),
        evaluation.totalScore(),
        evaluation.confidence(),
        sessionQuestion.getRoundNumber());

    return evaluation;
  }

  @Transactional
  @Override
  public NextQuestionResult decideNextQuestion(UUID sessionId, EvaluationResultDto evaluation) {
    var session = loadAndValidateInProgress(sessionId);

    int totalAsked = countAllQuestions(sessionId);
    var finishReason = evaluateFinishCondition(sessionId, totalAsked);
    if (finishReason.isPresent()) {
      finishSession(sessionId, finishReason.get());
      return null;
    }

    if (!hasQuestionBudget(session)) {
      finishSession(sessionId, InterviewFinishedReason.MAX_QUESTIONS_REACHED);
      return null;
    }

    var plannedResult = askNextPlannedQuestion(sessionId);
    if (plannedResult != null) {
      return plannedResult;
    }

    var currentQuestion = getCurrentQuestion(sessionId);
    if (evaluation.shouldAskFollowUp() && canAskFollowUp(sessionId, currentQuestion)) {
      return generateFollowUp(sessionId, currentQuestion, evaluation);
    }

    var weakestTopic = topicStateService.findWeakestTopic(sessionId);
    if (weakestTopic.isPresent()) {
      var result = askReinforcementQuestion(sessionId, weakestTopic.get());
      if (result != null) {
        return result;
      }
    }

    finishSession(sessionId, InterviewFinishedReason.COVERAGE_COMPLETED);
    return null;
  }

  @Transactional
  @Override
  public NextQuestionResult askFirstQuestion(UUID sessionId) {
    var existingQ = sQuestionRepository.findBySessionIdAndRoundNumber(sessionId, 1);
    if (existingQ.isPresent()) {
      var sq = existingQ.get();
      return toNextQuestionResult(sq, sq.getQuestionTextSnapshot(), "PRIMARY", null, false);
    }

    var session = loadAndValidateInProgress(sessionId);
    var interviewLanguage = resolveInterviewLanguage(session);

    var planned =
        pQuestionRepository
            .findFirstByInterviewSessionIdAndPlannedStatus(sessionId, PlannedStatus.PLANNED)
            .orElseThrow(
                () -> new IllegalStateException("No planned questions for session " + sessionId));

    planned.setPlannedStatus(PlannedStatus.ASKED);
    pQuestionRepository.save(planned);

    var q = planned.getQuestion();
    var askDto =
        new AskQuestionDto(
            q.getQuestionText(),
            q.getExpectedAnswer(),
            q.getTopic(),
            q.getSubtopic(),
            q.getDifficulty(),
            interviewLanguage);

    var questionText = aService.askQuestion(askDto);

    var sessionQuestion = new SessionQuestion();
    sessionQuestion.setSession(session);
    sessionQuestion.setQuestion(q);
    sessionQuestion.setRoundNumber(1);
    sessionQuestion.setTopic(q.getTopic());
    sessionQuestion.setSubtopic(q.getSubtopic());
    sessionQuestion.setDifficulty(q.getDifficulty());
    sessionQuestion.setQuestionTextSnapshot(questionText);
    sessionQuestion.setSelectionReason(SelectionReason.BASELINE_COVERAGE);
    sessionQuestion.setQuestionType(QuestionType.PRIMARY);
    sQuestionRepository.save(sessionQuestion);

    return toNextQuestionResult(sessionQuestion, questionText, "PRIMARY", null, false);
  }

  @Transactional
  @Override
  public void finishSession(UUID sessionId, InterviewFinishedReason reason) {
    var session = iSessionRepository.findById(sessionId).orElseThrow();
    session.setStatus(InterviewSessionStatus.COMPLETED);
    session.setFinishedReason(reason);
    session.setFinishedAt(Instant.now());
  }

  @Transactional
  @Override
  public void cancelSession(UUID sessionId) {
    var session = iSessionRepository.findById(sessionId).orElseThrow();
    if (session.getStatus() == InterviewSessionStatus.COMPLETED
        || session.getStatus() == InterviewSessionStatus.CANCELLED) {
      throw new IllegalStateException("Session %s is already finished".formatted(sessionId));
    }
    session.setStatus(InterviewSessionStatus.CANCELLED);
    session.setFinishedReason(InterviewFinishedReason.MANUAL_STOP);
    session.setFinishedAt(Instant.now());
  }

  private InterviewSession loadAndValidateInProgress(UUID sessionId) {
    var session = iSessionRepository.findById(sessionId).orElseThrow();
    if (session.getStatus() != InterviewSessionStatus.IN_PROGRESS) {
      throw new IllegalStateException(
          "Session %s is not in progress, current status: %s"
              .formatted(sessionId, session.getStatus()));
    }
    return session;
  }

  private Optional<InterviewFinishedReason> evaluateFinishCondition(
      UUID sessionId, int totalAsked) {
    var session = iSessionRepository.findById(sessionId).orElseThrow();
    var maxQuestions = session.getMaxQuestions();

    if (totalAsked >= maxQuestions) {
      return Optional.of(InterviewFinishedReason.MAX_QUESTIONS_REACHED);
    }

    if (!topicStateService.isSessionComplete(sessionId, totalAsked)) {
      return Optional.empty();
    }

    var sessionConfidence = topicStateService.calculateSessionConfidence(sessionId);
    if (sessionConfidence >= session.getTargetConfidence().doubleValue()) {
      return Optional.of(InterviewFinishedReason.CONFIDENCE_REACHED);
    }

    return Optional.of(InterviewFinishedReason.COVERAGE_COMPLETED);
  }

  private NextQuestionResult askReinforcementQuestion(UUID sessionId, String topic) {
    var session = loadAndValidateInProgress(sessionId);
    var interviewLanguage = resolveInterviewLanguage(session);
    var mastery = getTopicMastery(sessionId, topic);
    var difficulty = resolveDynamicDifficulty(mastery);
    var excludeIds = getAskedExternalIds(sessionId);

    var questionOpt = qService.findReinforcementQuestion(topic, difficulty, excludeIds);
    if (questionOpt.isEmpty()) {
      log.info("No reinforcement question available for topic={}", topic);
      return null;
    }

    var q = questionOpt.get();
    var askDto =
        new AskQuestionDto(
            q.getQuestionText(),
            q.getExpectedAnswer(),
            q.getTopic(),
            q.getSubtopic(),
            q.getDifficulty(),
            interviewLanguage);

    var questionText = aService.askQuestion(askDto);
    var nextRound = getLastRoundNumber(sessionId) + 1;

    var sessionQuestion = new SessionQuestion();
    sessionQuestion.setSession(session);
    sessionQuestion.setQuestion(q);
    sessionQuestion.setRoundNumber(nextRound);
    sessionQuestion.setTopic(q.getTopic());
    sessionQuestion.setSubtopic(q.getSubtopic());
    sessionQuestion.setDifficulty(q.getDifficulty());
    sessionQuestion.setQuestionTextSnapshot(questionText);
    sessionQuestion.setSelectionReason(SelectionReason.WEAK_TOPIC_REINFORCEMENT);
    sessionQuestion.setQuestionType(QuestionType.PRIMARY);
    sQuestionRepository.save(sessionQuestion);

    return toNextQuestionResult(sessionQuestion, questionText, "PRIMARY", null, false);
  }

  private NextQuestionResult askNextPlannedQuestion(UUID sessionId) {
    var session = loadAndValidateInProgress(sessionId);
    var planned =
        pQuestionRepository
            .findFirstByInterviewSessionIdAndPlannedStatus(sessionId, PlannedStatus.PLANNED)
            .orElse(null);

    if (planned == null) {
      return null;
    }

    planned.setPlannedStatus(PlannedStatus.ASKED);
    pQuestionRepository.save(planned);

    var q = planned.getQuestion();
    var nextRound = getLastRoundNumber(sessionId) + 1;

    var askDto =
        new AskQuestionDto(
            q.getQuestionText(),
            q.getExpectedAnswer(),
            q.getTopic(),
            q.getSubtopic(),
            q.getDifficulty(),
            resolveInterviewLanguage(session));

    var questionText = aService.askQuestion(askDto);

    var sessionQuestion = new SessionQuestion();
    sessionQuestion.setSession(session);
    sessionQuestion.setQuestion(q);
    sessionQuestion.setRoundNumber(nextRound);
    sessionQuestion.setTopic(q.getTopic());
    sessionQuestion.setSubtopic(q.getSubtopic());
    sessionQuestion.setDifficulty(q.getDifficulty());
    sessionQuestion.setQuestionTextSnapshot(questionText);
    sessionQuestion.setSelectionReason(SelectionReason.BASELINE_COVERAGE);
    sessionQuestion.setQuestionType(QuestionType.PRIMARY);
    sQuestionRepository.save(sessionQuestion);

    return toNextQuestionResult(sessionQuestion, questionText, "PRIMARY", null, false);
  }

  private SessionQuestion getCurrentQuestion(UUID sessionId) {
    var questions =
        sQuestionRepository.findBySessionIdAndParentQuestionIsNullOrderByRoundNumberAsc(sessionId);
    if (questions.isEmpty()) {
      throw new IllegalStateException("No primary questions found for session " + sessionId);
    }
    return questions.get(questions.size() - 1);
  }

  private boolean canAskFollowUp(UUID sessionId, SessionQuestion primaryQuestion) {
    var session = iSessionRepository.findById(sessionId).orElseThrow();
    var maxFollowUps = session.getMaxFollowUpsPerPrimary();
    var currentFollowUps = sQuestionRepository.countByParentQuestionId(primaryQuestion.getId());
    return currentFollowUps < maxFollowUps;
  }

  private NextQuestionResult generateFollowUp(
      UUID sessionId, SessionQuestion primaryQuestion, EvaluationResultDto evaluation) {
    var session = loadAndValidateInProgress(sessionId);

    var followUpDto =
        new FollowUpQuestionDto(
            primaryQuestion.getQuestionTextSnapshot(),
            primaryQuestion.getQuestion() != null
                ? primaryQuestion.getQuestion().getExpectedAnswer()
                : "",
            "",
            evaluation.knowledgeGaps(),
            resolveInterviewLanguage(session));

    var followUpText = aService.generateFollowUp(followUpDto);

    var sessionQuestion = new SessionQuestion();
    sessionQuestion.setSession(session);
    sessionQuestion.setParentQuestion(primaryQuestion);
    sessionQuestion.setRoundNumber(getLastRoundNumber(sessionId) + 1);
    sessionQuestion.setTopic(primaryQuestion.getTopic());
    sessionQuestion.setSubtopic(primaryQuestion.getSubtopic());
    sessionQuestion.setDifficulty(primaryQuestion.getDifficulty());
    sessionQuestion.setQuestionTextSnapshot(followUpText);
    sessionQuestion.setSelectionReason(SelectionReason.FOLLOW_UP_CLARIFICATION);
    sessionQuestion.setQuestionType(QuestionType.FOLLOW_UP);
    sQuestionRepository.save(sessionQuestion);

    return toNextQuestionResult(
        sessionQuestion, followUpText, "FOLLOW_UP", evaluation.candidateFeedback(), true);
  }

  private int getLastRoundNumber(UUID sessionId) {
    var allQuestions =
        sQuestionRepository.findAll().stream()
            .filter(q -> q.getSession().getId().equals(sessionId))
            .toList();
    if (allQuestions.isEmpty()) {
      return 0;
    }
    return allQuestions.stream().mapToInt(SessionQuestion::getRoundNumber).max().orElse(0);
  }

  private int countAllQuestions(UUID sessionId) {
    return Math.toIntExact(sQuestionRepository.countBySessionId(sessionId));
  }

  private boolean hasQuestionBudget(InterviewSession session) {
    return countAllQuestions(session.getId()) < session.getMaxQuestions();
  }

  private NextQuestionResult toNextQuestionResult(
      SessionQuestion sessionQuestion,
      String questionText,
      String questionType,
      String candidateFeedback,
      boolean isFollowUp) {
    var session = sessionQuestion.getSession();
    int maxQuestions = session.getMaxQuestions();
    int remainingQuestions = Math.max(0, maxQuestions - countAllQuestions(session.getId()));
    return new NextQuestionResult(
        sessionQuestion.getId(),
        questionText,
        sessionQuestion.getRoundNumber(),
        maxQuestions,
        remainingQuestions,
        questionType,
        candidateFeedback,
        isFollowUp);
  }

  private double getTopicMastery(UUID sessionId, String topic) {
    return topicStateService.getTopicSummaries(sessionId).stream()
        .filter(s -> s.topic().equals(topic))
        .mapToDouble(s -> s.masteryScore().doubleValue())
        .findFirst()
        .orElse(0.0);
  }

  private Difficulty resolveDynamicDifficulty(double masteryScore) {
    if (masteryScore < 0.4) {
      return Difficulty.EASY;
    }
    if (masteryScore < 0.7) {
      return Difficulty.MEDIUM;
    }
    return Difficulty.HARD;
  }

  private List<String> getAskedExternalIds(UUID sessionId) {
    return sQuestionRepository
        .findBySessionIdAndParentQuestionIsNullOrderByRoundNumberAsc(sessionId)
        .stream()
        .map(SessionQuestion::getQuestion)
        .filter(Objects::nonNull)
        .map(Question::getExternalId)
        .toList();
  }

  private String serializeKnowledgeGaps(List<String> gaps) {
    if (gaps == null || gaps.isEmpty()) {
      return "[]";
    }
    return "[" + String.join(",", gaps.stream().map(s -> "\"" + escapeJson(s) + "\"").toList()) + "]";
  }

  private EvaluationResultDto normalizeEvaluation(EvaluationResultDto evaluation) {
    return new EvaluationResultDto(
        normalizeScore(evaluation.correctnessScore()),
        normalizeScore(evaluation.depthScore()),
        normalizeScore(evaluation.practicalScore()),
        normalizeScore(evaluation.totalScore()),
        normalizeScore(evaluation.confidence()),
        evaluation.knowledgeGaps() != null ? evaluation.knowledgeGaps() : List.of(),
        evaluation.strengths() != null ? evaluation.strengths() : List.of(),
        evaluation.shouldAskFollowUp(),
        evaluation.followUpFocus(),
        evaluation.candidateFeedback(),
        false);
  }

  private BigDecimal toScoreValue(double score) {
    return BigDecimal.valueOf(clamp01(score)).setScale(2, RoundingMode.HALF_UP);
  }

  private double normalizeScore(double score) {
    if (!Double.isFinite(score)) {
      return 0.0;
    }
    if (score > 1.0) {
      return clamp01(score / 10.0);
    }
    return clamp01(score);
  }

  private double clamp01(double value) {
    return Math.max(0.0, Math.min(1.0, value));
  }

  private String escapeJson(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private String resolveInterviewLanguage(InterviewSession session) {
    var language = session.getInterviewLanguage();
    return language == null || language.isBlank() ? "Russian" : language;
  }
}
