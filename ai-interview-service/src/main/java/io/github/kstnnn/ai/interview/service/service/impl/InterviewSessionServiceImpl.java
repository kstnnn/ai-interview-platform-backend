package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.dto.StartInterviewSessionDto;
import io.github.kstnnn.ai.interview.service.model.InterviewLevel;
import io.github.kstnnn.ai.interview.service.model.InterviewSession;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionStatus;
import io.github.kstnnn.ai.interview.service.service.InterviewSessionService;
import io.github.kstnnn.ai.interview.service.service.InterviewSessionTechnologyService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewSessionServiceImpl implements InterviewSessionService {

  private final InterviewSessionTechnologyService iService;

  @Override
  public void startInterview(StartInterviewSessionDto sessionDto) {
    var targetConfidence = estimateTargetConfidence(sessionDto.interviewLevel());
    var interviewSession = createInterviewSession(sessionDto, targetConfidence);
    iService.saveSessionTechnologies(sessionDto.technologies(), interviewSession);
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
