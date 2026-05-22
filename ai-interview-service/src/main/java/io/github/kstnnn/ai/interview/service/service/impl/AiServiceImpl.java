package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.dto.AskQuestionDto;
import io.github.kstnnn.ai.interview.service.dto.EvaluationResultDto;
import io.github.kstnnn.ai.interview.service.dto.FollowUpQuestionDto;
import io.github.kstnnn.ai.interview.service.dto.GreetingDto;
import io.github.kstnnn.ai.interview.service.service.AiService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

  private final ChatClient chatClient;

  @Value("classpath:/prompts/greeting-prompt.st")
  private Resource greetingResource;

  @Value("classpath:/prompts/evaluation-prompt.st")
  private Resource evaluationResource;

  @Value("classpath:/prompts/follow-up-question-prompt.st")
  private Resource followUpResource;

  @Override
  public String greeting(GreetingDto dto) {
    return stripThinkTags(chatClient
        .prompt()
        .user(
            u ->
                u.text(greetingResource)
                    .params(
                        Map.of(
                            "candidate_name", dto.candidateName() != null ? dto.candidateName() : "Candidate",
                            "technologies", dto.technologies() != null ? String.join(", ", dto.technologies()) : "",
                            "interview_level", dto.interviewLevel() != null ? dto.interviewLevel().toString() : "UNKNOWN",
                            "interview_language", dto.interviewLanguage() != null ? dto.interviewLanguage() : "English")))
        .call()
        .content());
  }

  @Override
  public String askQuestion(AskQuestionDto dto) {
    return stripThinkTags(chatClient
        .prompt()
        .system(
            sp ->
                sp.params(
                    Map.of(
                        "technologies", dto.topic(),
                        "interview_language", dto.interviewLanguage())))
        .user(
            """
            You are asking a technical interview question.
            Original question: {question}
            Expected answer: {expected_answer}
            Topic: {topic}
            Difficulty: {difficulty}

            Reformulate this question naturally as if speaking to the candidate.
            Do NOT change the meaning or difficulty.
            Ask only this ONE question. Output only the question text.
            """
                .formatted(
                    dto.questionText(),
                    dto.expectedAnswer(),
                    dto.topic(),
                    dto.difficulty().toString()))
        .call()
        .content());
  }

  @Override
  public EvaluationResultDto evaluateAnswer(
      String question, String expectedAnswer, String candidateAnswer) {
    return chatClient
        .prompt()
        .system("You are a strict technical interview evaluator. Return structured JSON only.")
        .user(
            u ->
                u.text(evaluationResource)
                    .params(
                        Map.of(
                            "question", question,
                            "expected_answer", expectedAnswer,
                            "candidate_answer", candidateAnswer)))
        .call()
        .entity(EvaluationResultDto.class);
  }

  @Override
  public String generateFollowUp(FollowUpQuestionDto dto) {
    return stripThinkTags(chatClient
        .prompt()
        .user(
            u ->
                u.text(followUpResource)
                    .params(
                        Map.of(
                            "interview_language", dto.interviewLanguage(),
                            "primary_question", dto.primaryQuestion(),
                            "expected_answer", dto.expectedAnswer(),
                            "candidate_answer", dto.candidateAnswer(),
                            "knowledge_gaps", dto.knowledgeGapsText())))
        .call()
        .content());
  }

  private String stripThinkTags(String content) {
    if (content == null) return "";
    return content.replaceAll("<think>.*?</think>", "").trim();
  }
}
