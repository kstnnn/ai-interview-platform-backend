package io.github.kstnnn.ai.interview.service.controller;

import io.github.kstnnn.ai.interview.service.dto.FollowUpQuestionRequestDto;
import io.github.kstnnn.ai.interview.service.service.AiSessionService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiController {

  private final ChatClient chatClient;
  private final AiSessionService aiSessionService;

  public AiController(ChatClient chatClient, AiSessionService aiSessionService) {
    this.chatClient = chatClient;
    this.aiSessionService = aiSessionService;
  }

  @GetMapping("/api/v1/ai")
  public Map<String, String> askAi(
      @RequestParam(value = "message", defaultValue = "Hello, I'm Ilya") String message,
      @RequestParam(value = "stack", defaultValue = "general") String stack) {
    return Map.of(
        "completion",
        this.chatClient
            .prompt()
            .system(sp -> sp.param("stack", stack))
            .user(message)
            .call()
            .content());
  }

  @PostMapping("/api/v1/ai/follow-up")
  public Map<String, String> generateFollowUpQuestion(
      @Valid @RequestBody FollowUpQuestionRequestDto request) {
    return Map.of(
        "followUpQuestion",
        aiSessionService.generateFollowUpQuestion(
            request.primaryQuestion(),
            request.expectedAnswer(),
            request.candidateAnswer(),
            request.knowledgeGaps(),
            request.interviewLanguage()));
  }
}
