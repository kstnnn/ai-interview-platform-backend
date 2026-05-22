package io.github.kstnnn.ai.interview.service.controller;

import io.github.kstnnn.ai.interview.service.dto.GreetingDto;
import io.github.kstnnn.ai.interview.service.dto.NextQuestionResult;
import io.github.kstnnn.ai.interview.service.dto.SubmitAnswerDto;
import io.github.kstnnn.ai.interview.service.service.AiService;
import io.github.kstnnn.ai.interview.service.service.InterviewFlowService;
import io.github.kstnnn.ai.interview.service.service.TopicStateService;
import java.util.Map;
import java.util.UUID;
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

  private final InterviewFlowService iFlowService;
  private final TopicStateService topicStateService;
  private final AiService aService;
  private final SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/interviews/{sessionId}/start")
  public void startSession(
      @DestinationVariable UUID sessionId, @Payload GreetingDto dto) {
    try {
      log.info("Starting session {}", sessionId);
      iFlowService.startSession(sessionId);

      var greeting = aService.greeting(dto);
      publishEvent(sessionId, "GREETING", Map.of("text", greeting));
    } catch (Exception e) {
      log.error("Failed to start session {}", sessionId, e);
      publishError(sessionId, "Failed to start session: " + e.getMessage());
    }
  }

  @MessageMapping("/interviews/{sessionId}/ready")
  public void onCandidateReady(@DestinationVariable UUID sessionId) {
    try {
      log.info("Candidate ready for session {}", sessionId);
      var result = iFlowService.askFirstQuestion(sessionId, "English");

        publishEvent(
            sessionId,
            "QUESTION_ASKED",
            Map.of(
                "sessionQuestionId", result.sessionQuestionId(),
                "roundNumber", result.roundNumber(),
                "questionType", result.questionType(),
                "text", result.questionText()));
    } catch (Exception e) {
      log.error("Failed to ask first question for session {}", sessionId, e);
      publishError(sessionId, "Failed to start interview: " + e.getMessage());
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
              "totalScore", evaluation.totalScore(),
              "feedback", evaluation.candidateFeedback()));

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

      if (nextResult.isFollowUp() && nextResult.candidateFeedback() != null) {
        publishEvent(
            sessionId,
            "FEEDBACK",
            Map.of("text", nextResult.candidateFeedback()));
      }

      publishEvent(
          sessionId,
          "QUESTION_ASKED",
          Map.of(
              "sessionQuestionId", nextResult.sessionQuestionId(),
              "roundNumber", nextResult.roundNumber(),
              "questionType", nextResult.questionType(),
              "text", nextResult.questionText()));
    } catch (Exception e) {
      log.error("Failed to process answer for session {}", sessionId, e);
      publishError(sessionId, "Failed to process answer: " + e.getMessage());
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

  public record InterviewEvent(String type, Map<String, Object> payload) {}
}
