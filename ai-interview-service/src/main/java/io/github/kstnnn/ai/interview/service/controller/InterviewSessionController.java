package io.github.kstnnn.ai.interview.service.controller;

import io.github.kstnnn.ai.interview.service.dto.InterviewHistoryDto;
import io.github.kstnnn.ai.interview.service.dto.InterviewSessionSummaryDto;
import io.github.kstnnn.ai.interview.service.dto.LearningRoadmapDto;
import io.github.kstnnn.ai.interview.service.dto.InterviewReportDto;
import io.github.kstnnn.ai.interview.service.dto.SessionCreatedResponse;
import io.github.kstnnn.ai.interview.service.dto.StartInterviewSessionDto;
import io.github.kstnnn.ai.interview.service.dto.UserLearningRoadmapDto;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionStatus;
import io.github.kstnnn.ai.interview.service.service.InterviewReportService;
import io.github.kstnnn.ai.interview.service.service.InterviewSessionService;
import io.github.kstnnn.ai.interview.service.service.LearningRoadmapService;
import io.github.kstnnn.ai.interview.service.service.UserLookupService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
@Slf4j
public class InterviewSessionController {

  private final InterviewSessionService iSessionService;
  private final InterviewReportService iReportService;
  private final LearningRoadmapService learningRoadmapService;
  private final UserLookupService userLookupService;

  @PostMapping
  public ResponseEntity<SessionCreatedResponse> createSession(
      @AuthenticationPrincipal Jwt jwt, @RequestBody StartInterviewSessionDto dto) {
    var resolvedDto = resolveUserId(jwt, dto);
    var sessionId = iSessionService.initSession(resolvedDto);
    var response =
        new SessionCreatedResponse(
            sessionId,
            InterviewSessionStatus.CREATED,
            resolvedDto.interviewLevel(),
            resolveInterviewLanguage(resolvedDto.interviewLanguage()),
            resolvedDto.minQuestions(),
            resolvedDto.maxQuestions(),
            resolvedDto.technologyKeys(),
            Instant.now());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{sessionId}/report")
  public InterviewReportDto getReport(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID sessionId) {
    var userId = userLookupService.resolveInternalUserId(jwt.getSubject());
    return iReportService.getMockReport(sessionId, userId);
  }

  @GetMapping("/learning-roadmap")
  public UserLearningRoadmapDto getUserLearningRoadmap(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(required = false) String language,
      @RequestHeader(name = "Accept-Language", required = false) String acceptLanguage) {
    var userId = userLookupService.resolveInternalUserId(jwt.getSubject());
    var requestedLanguage = language != null && !language.isBlank() ? language : acceptLanguage;
    return learningRoadmapService.getUserRoadmap(userId, requestedLanguage);
  }

  @GetMapping("/{sessionId}/learning-roadmap")
  public LearningRoadmapDto getLearningRoadmap(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID sessionId,
      @RequestParam(required = false) String language,
      @RequestHeader(name = "Accept-Language", required = false) String acceptLanguage) {
    var userId = userLookupService.resolveInternalUserId(jwt.getSubject());
    var requestedLanguage = language != null && !language.isBlank() ? language : acceptLanguage;
    return learningRoadmapService.getRoadmap(sessionId, userId, requestedLanguage);
  }

  @GetMapping("/{sessionId}")
  public InterviewSessionSummaryDto getSession(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID sessionId) {
    var userId = userLookupService.resolveInternalUserId(jwt.getSubject());
    return iSessionService.getSessionSummary(sessionId, userId);
  }

  @GetMapping("/my")
  public List<InterviewHistoryDto> getMyInterviews(@AuthenticationPrincipal Jwt jwt) {
    var userId = userLookupService.resolveInternalUserId(jwt.getSubject());
    return iSessionService.getLatestSessions(userId);
  }

  private StartInterviewSessionDto resolveUserId(Jwt jwt, StartInterviewSessionDto dto) {
    if (jwt == null) {
      return dto;
    }
    return new StartInterviewSessionDto(
        userLookupService.resolveInternalUserId(jwt.getSubject()),
        dto.vacancyId(),
        dto.applicationId(),
        dto.minQuestions(),
        dto.maxQuestions(),
        dto.maxFollowUpsPerPrimary(),
        dto.interviewLevel(),
        dto.interviewLanguage(),
        dto.technologyKeys(),
        dto.customQuestions());
  }

  private String resolveInterviewLanguage(String interviewLanguage) {
    return interviewLanguage == null || interviewLanguage.isBlank() ? "Russian" : interviewLanguage;
  }
}
