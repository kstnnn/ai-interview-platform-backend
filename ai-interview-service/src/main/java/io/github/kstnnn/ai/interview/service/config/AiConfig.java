package io.github.kstnnn.ai.interview.service.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

  @Bean
  ChatClient chatClient(ChatClient.Builder builder) {
    return builder
        .defaultSystem(
            "You're interviewer that have to interview candidates. Stack {stack}. Level middle.")
        .build();
  }
}
