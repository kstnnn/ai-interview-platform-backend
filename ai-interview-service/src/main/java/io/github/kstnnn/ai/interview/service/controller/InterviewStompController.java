package io.github.kstnnn.ai.interview.service.controller;

import io.github.kstnnn.ai.interview.service.dto.GreetingDto;
import io.github.kstnnn.ai.interview.service.dto.NextQuestionResult;
import io.github.kstnnn.ai.interview.service.dto.SubmitAnswerDto;
import io.github.kstnnn.ai.interview.service.service.AiService;
import io.github.kstnnn.ai.interview.service.service.InterviewFlowService;
import io.github.kstnnn.ai.interview.service.service.TopicStateService;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class InterviewStompController {

  private static final Pattern RETRY_AFTER_PATTERN = Pattern.compile("try again in ([0-9.]+)s");

  private final InterviewFlowService iFlowService;
  private final TopicStateService topicStateService;
  private final AiService aService;
  private final SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/interviews/{sessionId}/start")
  public void startSession(
      @DestinationVariable UUID sessionId, @Payload GreetingDto dto) {
    try {
      log.info("Starting session {}", sessionId);
      iFlowService.startSession(sessionId, dto.interviewLanguage());

      var greeting = aService.greeting(dto);
      publishEvent(sessionId, "GREETING", Map.of("text", greeting));
    } catch (Exception e) {
      log.error("Failed to start session {}", sessionId, e);
      publishException(sessionId, "Failed to start session", e);
    }
  }

  @MessageMapping("/interviews/{sessionId}/ready")
  public void onCandidateReady(@DestinationVariable UUID sessionId) {
    try {
      log.info("Candidate ready for session {}", sessionId);
      var result = iFlowService.askFirstQuestion(sessionId);

        publishEvent(
            sessionId,
            "QUESTION_ASKED",
            Map.of(
                "sessionQuestionId", result.sessionQuestionId(),
                "roundNumber", result.roundNumber(),
                "questionIndex", result.roundNumber(),
                "maxQuestions", result.maxQuestions(),
                "remainingQuestions", result.remainingQuestions(),
                "questionType", result.questionType(),
                "text", result.questionText()));
    } catch (Exception e) {
      log.error("Failed to ask first question for session {}", sessionId, e);
      publishException(sessionId, "Failed to start interview", e);
    }
  }

  @MessageMapping("/interviews/{sessionId}/answers")
  public void submitAnswer(
      @DestinationVariable UUID sessionId, @Payload SubmitAnswerDto dto) {
    try {
      log.info("Answer submitted for session {} question {}", sessionId, dto.sessionQuestionId());

      var evaluation = iFlowService.submitAnswer(sessionId, dto);

      publishEvent(
          sessionId,
          "ANSWER_EVALUATED",
          Map.of(
              "totalScore", evaluation.totalScore()));

      if (evaluation.duplicateSubmission()) {
        return;
      }

      var nextResult = iFlowService.decideNextQuestion(sessionId, evaluation);

      if (nextResult == null) {
        var sessionConfidence = topicStateService.calculateSessionConfidence(sessionId);
        var topicSummaries = topicStateService.getTopicSummaries(sessionId);
        publishEvent(
            sessionId,
            "SESSION_FINISHED",
            Map.of(
                "reason", "INTERVIEW_COMPLETE",
                "sessionConfidence", sessionConfidence,
                "topics", topicSummaries));
        return;
      }

      publishEvent(
          sessionId,
          "QUESTION_ASKED",
          Map.of(
              "sessionQuestionId", nextResult.sessionQuestionId(),
              "roundNumber", nextResult.roundNumber(),
              "questionIndex", nextResult.roundNumber(),
              "maxQuestions", nextResult.maxQuestions(),
              "remainingQuestions", nextResult.remainingQuestions(),
              "questionType", nextResult.questionType(),
              "text", nextResult.questionText()));
    } catch (Exception e) {
      log.error("Failed to process answer for session {}", sessionId, e);
      publishException(sessionId, "Failed to process answer", e);
    }
  }

  @MessageMapping("/interviews/{sessionId}/leave")
  public void leaveSession(@DestinationVariable UUID sessionId) {
    try {
      log.info("Candidate leaving session {}", sessionId);
      iFlowService.cancelSession(sessionId);
      publishEvent(sessionId, "SESSION_FINISHED", Map.of("reason", "MANUAL_LEAVE"));
    } catch (Exception e) {
      log.error("Failed to cancel session {}", sessionId, e);
      publishError(sessionId, "Failed to leave session: " + e.getMessage());
    }
  }

  private void publishEvent(UUID sessionId, String type, Map<String, Object> payload) {
    var destination = "/topic/interviews/" + sessionId + "/events";
    messagingTemplate.convertAndSend(destination, new InterviewEvent(type, payload));
  }

  private void publishError(UUID sessionId, String message) {
    var destination = "/topic/interviews/" + sessionId + "/events";
    messagingTemplate.convertAndSend(destination, new InterviewEvent("ERROR", Map.of("message", message)));
  }

  private void publishException(UUID sessionId, String fallbackMessage, Exception exception) {
    if (isRateLimit(exception)) {
      publishEvent(
          sessionId,
          "ERROR",
          Map.of(
              "code", "AI_RATE_LIMIT",
              "message", "AI provider rate limit reached. Please wait a few seconds and retry.",
              "retryAfterSeconds", extractRetryAfterSeconds(exception)));
      return;
    }
    publishError(sessionId, fallbackMessage + ": " + exception.getMessage());
  }

  private boolean isRateLimit(Throwable throwable) {
    while (throwable != null) {
      var message = throwable.getMessage();
      if (message != null
          && (message.contains("HTTP 429") || message.toLowerCase().contains("rate limit"))) {
        return true;
      }
      throwable = throwable.getCause();
    }
    return false;
  }

  private double extractRetryAfterSeconds(Throwable throwable) {
    while (throwable != null) {
      var message = throwable.getMessage();
      if (message != null) {
        var matcher = RETRY_AFTER_PATTERN.matcher(message);
        if (matcher.find()) {
          return Double.parseDouble(matcher.group(1));
        }
      }
      throwable = throwable.getCause();
    }
    return 5.0;
  }

  public record InterviewEvent(String type, Map<String, Object> payload) {}
}
