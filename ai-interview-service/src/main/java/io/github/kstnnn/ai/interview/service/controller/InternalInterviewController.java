package io.github.kstnnn.ai.interview.service.controller;

import io.github.kstnnn.ai.interview.service.dto.InterviewReportDto;
import io.github.kstnnn.ai.interview.service.service.InterviewReportService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/interviews")
@RequiredArgsConstructor
public class InternalInterviewController {

  private final InterviewReportService interviewReportService;

  @Value("${app.internal-token:local-internal-token}")
  private String internalToken;

  @GetMapping("/{sessionId}/report")
  public InterviewReportDto getReport(
      @PathVariable UUID sessionId,
      @RequestHeader(name = "X-Internal-Token", required = false) String token) {
    if (!internalToken.equals(token)) {
      throw new IllegalArgumentException("Invalid internal token");
    }
    return interviewReportService.getInternalReport(sessionId);
  }
}
