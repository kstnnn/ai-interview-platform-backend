package io.github.kstnnn.ai.interview.service.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AiController {

  private final ChatClient chatClient;

  @GetMapping()
  public Map<String, String> greetingCandidate(
      @RequestParam(value = "message", defaultValue = "Hello, I'm Ilya") String message,
      @RequestParam(value = "stack", defaultValue = "general") String stack,
      @RequestParam(value = "interviewLanguage", defaultValue = "English")
          String interviewLanguage) {
    return Map.of(
        "completion",
        this.chatClient
            .prompt()
            .system(sp -> sp.param("stack", stack).param("interview_language", interviewLanguage))
            .user(message)
            .call()
            .content());
  }
}
