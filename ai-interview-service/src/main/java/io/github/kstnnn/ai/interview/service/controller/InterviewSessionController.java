package io.github.kstnnn.ai.interview.service.controller;

import io.github.kstnnn.ai.interview.service.dto.AnswerSubmissionResponseDto;
import io.github.kstnnn.ai.interview.service.dto.InterviewHistoryDto;
import io.github.kstnnn.ai.interview.service.dto.InterviewSessionSummaryDto;
import io.github.kstnnn.ai.interview.service.dto.LearningRoadmapDto;
import io.github.kstnnn.ai.interview.service.dto.InterviewReportDto;
import io.github.kstnnn.ai.interview.service.dto.SubmitAnswerDto;
import io.github.kstnnn.ai.interview.service.dto.SessionCreatedResponse;
import io.github.kstnnn.ai.interview.service.dto.StartInterviewSessionDto;
import io.github.kstnnn.ai.interview.service.dto.UserLearningRoadmapDto;
import io.github.kstnnn.ai.interview.service.dto.VoiceAnswerResponseDto;
import io.github.kstnnn.ai.interview.service.dto.VoiceSynthesisRequestDto;
import io.github.kstnnn.ai.interview.service.dto.VoiceTranscriptionResponseDto;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionStatus;
import io.github.kstnnn.ai.interview.service.repository.InterviewSessionTechnologyRepository;
import io.github.kstnnn.ai.interview.service.service.InterviewFlowService;
import io.github.kstnnn.ai.interview.service.service.InterviewReportService;
import io.github.kstnnn.ai.interview.service.service.InterviewSessionService;
import io.github.kstnnn.ai.interview.service.service.LearningRoadmapService;
import io.github.kstnnn.ai.interview.service.service.TopicStateService;
import io.github.kstnnn.ai.interview.service.service.UserLookupService;
import io.github.kstnnn.ai.interview.service.service.VoiceService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
@Slf4j
public class InterviewSessionController {

  private final InterviewSessionService iSessionService;
  private final InterviewFlowService iFlowService;
  private final InterviewReportService iReportService;
  private final LearningRoadmapService learningRoadmapService;
  private final TopicStateService topicStateService;
  private final UserLookupService userLookupService;
  private final VoiceService voiceService;
  private final InterviewSessionTechnologyRepository iSessionTechnologyRepository;

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

  @PostMapping("/{sessionId}/answers")
  public AnswerSubmissionResponseDto submitAnswer(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID sessionId,
      @Valid @RequestBody SubmitAnswerDto dto) {
    var userId = userLookupService.resolveInternalUserId(jwt.getSubject());
    iSessionService.getSessionSummary(sessionId, userId);
    return submitTextAnswer(sessionId, dto);
  }

  @PostMapping(value = "/{sessionId}/voice/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public VoiceTranscriptionResponseDto transcribeVoiceAnswer(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID sessionId,
      @RequestPart MultipartFile audio) {
    var userId = userLookupService.resolveInternalUserId(jwt.getSubject());
    iSessionService.getSessionSummary(sessionId, userId);

    var transcription = voiceService.transcribe(audio, buildSttInitialPrompt(sessionId));
    if (transcription.text() == null || transcription.text().isBlank()) {
      throw new IllegalArgumentException("Transcribed answer text is empty");
    }
    return transcription;
  }

  @PostMapping(value = "/{sessionId}/voice-answer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public VoiceAnswerResponseDto submitVoiceAnswer(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID sessionId,
      @RequestPart UUID sessionQuestionId,
      @RequestPart MultipartFile audio) {
    var userId = userLookupService.resolveInternalUserId(jwt.getSubject());
    iSessionService.getSessionSummary(sessionId, userId);

    var transcription = voiceService.transcribe(audio, buildSttInitialPrompt(sessionId));
    if (transcription.text() == null || transcription.text().isBlank()) {
      throw new IllegalArgumentException("Transcribed answer text is empty");
    }
    return VoiceAnswerResponseDto.fromSubmission(
        transcription.text(),
        transcription.language(),
        submitTextAnswer(sessionId, new SubmitAnswerDto(sessionQuestionId, transcription.text())));
  }

  @PostMapping(value = "/voice/synthesize", produces = "audio/wav")
  public ResponseEntity<byte[]> synthesizeVoice(
      @Valid @RequestBody VoiceSynthesisRequestDto request) {
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("audio/wav"))
        .body(voiceService.synthesize(request.text(), request.speaker()));
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

  private String buildSttInitialPrompt(UUID sessionId) {
    var technologies = iSessionTechnologyRepository.findTechnologyDisplayNamesBySessionId(sessionId);
    if (technologies.isEmpty()) {
      return "Техническое интервью. Возможны русские фразы с английскими техническими терминами.";
    }
    return "Техническое интервью. Возможные технологии и термины: "
        + String.join(", ", technologies)
        + ". Возможна русская речь с английскими техническими словами, названиями классов, фреймворков и аббревиатурами.";
  }

  private AnswerSubmissionResponseDto submitTextAnswer(UUID sessionId, SubmitAnswerDto dto) {
    var evaluation = iFlowService.submitAnswer(sessionId, dto);

    if (evaluation.duplicateSubmission()) {
      return new AnswerSubmissionResponseDto(evaluation, null, false, null, List.of());
    }

    var nextQuestion = iFlowService.decideNextQuestion(sessionId, evaluation);
    if (nextQuestion != null) {
      return new AnswerSubmissionResponseDto(evaluation, nextQuestion, false, null, List.of());
    }

    return new AnswerSubmissionResponseDto(
        evaluation,
        null,
        true,
        topicStateService.calculateSessionConfidence(sessionId),
        topicStateService.getTopicSummaries(sessionId));
  }
}
