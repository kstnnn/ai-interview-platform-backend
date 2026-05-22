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

  @Transactional
  @Override
  public UUID initSession(StartInterviewSessionDto sessionDto) {
    var targetConfidence = estimateTargetConfidence(sessionDto.interviewLevel());
    var interviewSession = iSessionRepository.save(createInterviewSession(sessionDto, targetConfidence));
    iSessionTechnologyService.saveSessionTechnologies(sessionDto.technologyKeys(), interviewSession);
    var questions = qService.getBaseQuestions(sessionDto.technologyKeys());
    pSessionQuestionService.savePlannedQuestions(questions, interviewSession);
    return interviewSession.getId();
  }

  @Transactional
  @Override
  public void cancelSession(UUID sessionId) {
    var session = iSessionRepository.findById(sessionId).orElseThrow();
    session.setStatus(InterviewSessionStatus.CANCELLED);
    session.setFinishedAt(Instant.now());
  }

  private InterviewSession createInterviewSession(
      StartInterviewSessionDto sessionDto, BigDecimal targetConfidence) {
    return InterviewSession.builder()
        .userId(sessionDto.userId())
        .status(InterviewSessionStatus.CREATED)
        .interviewLevel(sessionDto.interviewLevel())
        .interviewLanguage(resolveInterviewLanguage(sessionDto.interviewLanguage()))
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

  private String resolveInterviewLanguage(String interviewLanguage) {
    return interviewLanguage == null || interviewLanguage.isBlank() ? "Russian" : interviewLanguage;
  }
}
