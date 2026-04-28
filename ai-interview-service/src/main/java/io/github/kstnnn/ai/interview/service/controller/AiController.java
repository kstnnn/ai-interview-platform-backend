package io.github.kstnnn.ai.interview.service.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AiController {

  private final ChatClient chatClient;

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
}
