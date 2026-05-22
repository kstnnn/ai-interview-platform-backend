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
  public void startSession(UUID sessionId) {
    var session = iSessionRepository.findById(sessionId).orElseThrow();
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
    loadAndValidateInProgress(sessionId);

    var existingAnswer = sAnswerRepository.findBySessionQuestionId(dto.sessionQuestionId());
    if (existingAnswer.isPresent()) {
      log.warn("Answer already submitted for question {}, returning existing evaluation", dto.sessionQuestionId());
      var eval = aEvaluationRepository.findBySessionAnswerId(existingAnswer.get().getId()).orElseThrow();
      return new EvaluationResultDto(
          eval.getCorrectnessScore().intValue(),
          eval.getDepthScore().intValue(),
          eval.getPracticalScore().intValue(),
          eval.getTotalScore().intValue(),
          0.0,
          List.of(),
          List.of(),
          false,
          null,
          eval.getFeedback());
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
        aService.evaluateAnswer(
            sessionQuestion.getQuestionTextSnapshot(),
            sessionQuestion.getQuestion() != null
                ? sessionQuestion.getQuestion().getExpectedAnswer()
                : "",
            dto.answerText());

    var evaluationEntity = new AnswerEvaluation();
    evaluationEntity.setSessionAnswer(answer);
    evaluationEntity.setCorrectnessScore(BigDecimal.valueOf(evaluation.correctnessScore()));
    evaluationEntity.setDepthScore(BigDecimal.valueOf(evaluation.depthScore()));
    evaluationEntity.setPracticalScore(BigDecimal.valueOf(evaluation.practicalScore()));
    evaluationEntity.setTotalScore(BigDecimal.valueOf(evaluation.totalScore()));
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
    loadAndValidateInProgress(sessionId);

    int totalAsked = countPrimaryQuestions(sessionId);
    var finishReason = evaluateFinishCondition(sessionId, totalAsked);
    if (finishReason.isPresent()) {
      finishSession(sessionId, finishReason.get());
      return null;
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

    return askNextPlannedQuestion(sessionId);
  }

  @Transactional
  @Override
  public NextQuestionResult askFirstQuestion(UUID sessionId, String interviewLanguage) {
    var existingQ = sQuestionRepository.findBySessionIdAndRoundNumber(sessionId, 1);
    if (existingQ.isPresent()) {
      var sq = existingQ.get();
      return new NextQuestionResult(
          sq.getId(), sq.getQuestionTextSnapshot(), 1, "PRIMARY", null, false);
    }

    var session = loadAndValidateInProgress(sessionId);

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

    return new NextQuestionResult(sessionQuestion.getId(), questionText, 1, "PRIMARY", null, false);
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
            null);

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

    return new NextQuestionResult(
        sessionQuestion.getId(), questionText, nextRound, "PRIMARY", null, false);
  }

  private NextQuestionResult askNextPlannedQuestion(UUID sessionId) {
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
            null);

    var questionText = aService.askQuestion(askDto);

    var sessionQuestion = new SessionQuestion();
    sessionQuestion.setSession(loadAndValidateInProgress(sessionId));
    sessionQuestion.setQuestion(q);
    sessionQuestion.setRoundNumber(nextRound);
    sessionQuestion.setTopic(q.getTopic());
    sessionQuestion.setSubtopic(q.getSubtopic());
    sessionQuestion.setDifficulty(q.getDifficulty());
    sessionQuestion.setQuestionTextSnapshot(questionText);
    sessionQuestion.setSelectionReason(SelectionReason.BASELINE_COVERAGE);
    sessionQuestion.setQuestionType(QuestionType.PRIMARY);
    sQuestionRepository.save(sessionQuestion);

    return new NextQuestionResult(
        sessionQuestion.getId(), questionText, nextRound, "PRIMARY", null, false);
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

    var followUpDto =
        new FollowUpQuestionDto(
            primaryQuestion.getQuestionTextSnapshot(),
            primaryQuestion.getQuestion() != null
                ? primaryQuestion.getQuestion().getExpectedAnswer()
                : "",
            "",
            evaluation.knowledgeGaps(),
            null);

    var followUpText = aService.generateFollowUp(followUpDto);

    var sessionQuestion = new SessionQuestion();
    sessionQuestion.setSession(loadAndValidateInProgress(sessionId));
    sessionQuestion.setParentQuestion(primaryQuestion);
    sessionQuestion.setRoundNumber(primaryQuestion.getRoundNumber() + 1);
    sessionQuestion.setTopic(primaryQuestion.getTopic());
    sessionQuestion.setSubtopic(primaryQuestion.getSubtopic());
    sessionQuestion.setDifficulty(primaryQuestion.getDifficulty());
    sessionQuestion.setQuestionTextSnapshot(followUpText);
    sessionQuestion.setSelectionReason(SelectionReason.FOLLOW_UP_CLARIFICATION);
    sessionQuestion.setQuestionType(QuestionType.FOLLOW_UP);
    sQuestionRepository.save(sessionQuestion);

    return new NextQuestionResult(
        sessionQuestion.getId(),
        followUpText,
        sessionQuestion.getRoundNumber(),
        "FOLLOW_UP",
        evaluation.candidateFeedback(),
        true);
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

  private int countPrimaryQuestions(UUID sessionId) {
    return sQuestionRepository
        .findBySessionIdAndParentQuestionIsNullOrderByRoundNumberAsc(sessionId)
        .size();
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
    return "[" + String.join(",", gaps.stream().map(s -> "\"" + s + "\"").toList()) + "]";
  }
}
