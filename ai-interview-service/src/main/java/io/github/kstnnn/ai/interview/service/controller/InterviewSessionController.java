package io.github.kstnnn.ai.interview.service.controller;

import io.github.kstnnn.ai.interview.service.dto.SessionCreatedResponse;
import io.github.kstnnn.ai.interview.service.dto.StartInterviewSessionDto;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionStatus;
import io.github.kstnnn.ai.interview.service.service.InterviewSessionService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
@Slf4j
public class InterviewSessionController {

  private final InterviewSessionService iSessionService;

  @PostMapping
  public ResponseEntity<SessionCreatedResponse> createSession(
      @RequestBody StartInterviewSessionDto dto) {
    var sessionId = iSessionService.initSession(dto);
    var response =
        new SessionCreatedResponse(
            sessionId,
            InterviewSessionStatus.CREATED,
            dto.interviewLevel(),
            dto.minQuestions(),
            dto.maxQuestions(),
            dto.technologyKeys(),
            Instant.now());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
