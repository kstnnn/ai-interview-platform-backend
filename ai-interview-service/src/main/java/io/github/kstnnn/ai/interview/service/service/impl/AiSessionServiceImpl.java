package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.service.AiSessionService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
public class AiSessionServiceImpl implements AiSessionService {

  private final ChatClient chatClient;

  public AiSessionServiceImpl(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  @Value("classpath:/prompts/follow-up-question-prompt.st")
  private Resource followUpPrompt;

  @Override
  public String generateFollowUpQuestion(
      String primaryQuestion,
      String expectedAnswer,
      String candidateAnswer,
      String knowledgeGaps,
      String language) {
    String template = readPromptTemplate();
    String prompt =
        template
            .replace("{primary_question}", safe(primaryQuestion))
            .replace("{expected_answer}", safe(expectedAnswer))
            .replace("{candidate_answer}", safe(candidateAnswer))
            .replace("{knowledge_gaps}", safe(knowledgeGaps))
            .replace("{interview_language}", safe(language));

    return chatClient.prompt().user(prompt).call().content();
  }

  private String readPromptTemplate() {
    try {
      return StreamUtils.copyToString(followUpPrompt.getInputStream(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to load follow-up prompt template", e);
    }
  }

  private String safe(String value) {
    return value == null ? "" : value;
  }
}
