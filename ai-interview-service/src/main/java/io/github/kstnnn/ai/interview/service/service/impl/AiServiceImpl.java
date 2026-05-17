package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.dto.GreetingDto;
import io.github.kstnnn.ai.interview.service.service.AiService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

  private final ChatClient chatClient;

  @Override
  public String greeting(GreetingDto dto) {
    return chatClient
        .prompt()
        .system(
            sp ->
                sp.params(
                    Map.of(
                        "technologies",
                        dto.technologies(),
                        "interview_language",
                        dto.interviewLanguage(),
                        "candidate_name",
                        dto.candidateName(),
                        "interview_level",
                        dto.interviewLevel())))
        .call()
        .content();
  }

  @Override
  public void generateFollowUpQuestion() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'generateFollowUpQuestion'");
  }

  @Override
  public void askQuestion() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'askQuestion'");
  }

  @Override
  public void evaluateAnswer() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'evaluateAnswer'");
  }
}
