package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.dto.StartInterviewSessionDto;
import io.github.kstnnn.ai.interview.service.model.InterviewLevel;
import io.github.kstnnn.ai.interview.service.model.InterviewSession;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionStatus;
import io.github.kstnnn.ai.interview.service.repository.InterviewSessionRepository;
import io.github.kstnnn.ai.interview.service.service.InterviewSessionService;
import io.github.kstnnn.ai.interview.service.service.InterviewSessionTechnologyService;
import io.github.kstnnn.ai.interview.service.service.PlannedSessionQuestionService;
import io.github.kstnnn.ai.interview.service.service.QuestionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewSessionServiceImpl implements InterviewSessionService {

  private final InterviewSessionTechnologyService iSessionTechnologyService;
  private final QuestionService qService;
  private final PlannedSessionQuestionService pSessionQuestionService;
  private final InterviewSessionRepository iSessionRepository;

  @Override
  public void initSession(StartInterviewSessionDto sessionDto) {
    var targetConfidence = estimateTargetConfidence(sessionDto.interviewLevel());
    // Creating new session
    var interviewSession = createInterviewSession(sessionDto, targetConfidence);
    // Saving technologies of session
    iSessionTechnologyService.saveSessionTechnologies(sessionDto.technologies(), interviewSession);
    // Getting base questions
    var questions = qService.getBaseQuestions(sessionDto.technologies());
    // Saving base questions of Interview
    pSessionQuestionService.savePlannedQuestions(questions, interviewSession);
  }

  @Transactional
  @Override
  public void startSession(UUID sessionId) {
    var interviewSession = iSessionRepository.findById(sessionId).orElseThrow();
    interviewSession.setStatus(InterviewSessionStatus.IN_PROGRESS);
  }

  @Override
  @Transactional
  public void stopSession(UUID sessionId) {
    var session = iSessionRepository.getReferenceById(sessionId);
    session.setStatus(InterviewSessionStatus.CANCELLED);
    session.setFinishedAt(Instant.now());
  }

  private InterviewSession createInterviewSession(
      StartInterviewSessionDto sessionDto, BigDecimal targetConfidence) {
    return InterviewSession.builder()
        .userId(sessionDto.userId())
        .status(InterviewSessionStatus.CREATED)
        .interviewLevel(sessionDto.interviewLevel())
        .targetConfidence(targetConfidence)
        .minQuestions(sessionDto.minQuestions())
        .maxQuestions(sessionDto.maxQuestions())
        .build();
  }

  private BigDecimal estimateTargetConfidence(InterviewLevel interviewLevel) {
    return switch (interviewLevel) {
      case JUNIOR -> new BigDecimal("0.7");
      case MIDDLE -> new BigDecimal("0.8");
      case SENIOR -> new BigDecimal("0.9");
    };
  }
}
