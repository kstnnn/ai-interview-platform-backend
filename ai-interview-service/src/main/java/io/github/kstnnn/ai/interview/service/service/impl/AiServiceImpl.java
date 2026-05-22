package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.dto.AskQuestionDto;
import io.github.kstnnn.ai.interview.service.dto.AiEvaluationDto;
import io.github.kstnnn.ai.interview.service.dto.EvaluationResultDto;
import io.github.kstnnn.ai.interview.service.dto.FollowUpQuestionDto;
import io.github.kstnnn.ai.interview.service.dto.GreetingDto;
import io.github.kstnnn.ai.interview.service.service.AiService;
import java.util.List;
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

  @Value("classpath:/prompts/ask-question-prompt.st")
  private Resource askQuestionResource;

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
                            "interview_language", dto.interviewLanguage() != null ? dto.interviewLanguage() : "Russian")))
        .call()
        .content());
  }

  @Override
  public String askQuestion(AskQuestionDto dto) {
    return stripThinkTags(chatClient
        .prompt()
        .user(
            u ->
                u.text(askQuestionResource)
                    .params(
                        Map.of(
                            "question", defaultString(dto.questionText()),
                            "expected_answer", defaultString(dto.expectedAnswer()),
                            "topic", defaultString(dto.topic()),
                            "difficulty", dto.difficulty() != null ? dto.difficulty().toString() : "MEDIUM",
                            "interview_language", defaultString(dto.interviewLanguage(), "Russian"))))
        .call()
        .content());
  }

  @Override
  public EvaluationResultDto evaluateAnswer(
      String question, String expectedAnswer, String candidateAnswer, String interviewLanguage) {
    var evaluation = chatClient
        .prompt()
        .system("You are a strict technical interview evaluator. Return structured JSON only.")
        .user(
            u ->
                u.text(evaluationResource)
                    .params(
                        Map.of(
                            "question", defaultString(question),
                            "expected_answer", defaultString(expectedAnswer),
                            "candidate_answer", defaultString(candidateAnswer),
                            "interview_language", defaultString(interviewLanguage, "Russian"))))
        .call()
        .entity(AiEvaluationDto.class);

    return toEvaluationResult(evaluation);
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
                            "interview_language", defaultString(dto.interviewLanguage(), "Russian"),
                            "primary_question", defaultString(dto.primaryQuestion()),
                            "expected_answer", defaultString(dto.expectedAnswer()),
                            "candidate_answer", defaultString(dto.candidateAnswer()),
                            "knowledge_gaps", defaultString(dto.knowledgeGapsText()))))
        .call()
        .content());
  }

  private String stripThinkTags(String content) {
    if (content == null) return "";
    return content.replaceAll("(?is)<think>.*?</think>", "").trim();
  }

  private String defaultString(String value) {
    return defaultString(value, "");
  }

  private String defaultString(String value, String fallback) {
    return value != null ? value : fallback;
  }

  private EvaluationResultDto toEvaluationResult(AiEvaluationDto evaluation) {
    double correctnessScore = defaultScore(evaluation.correctnessScore());
    double depthScore = defaultScore(evaluation.depthScore());
    double practicalScore = defaultScore(evaluation.practicalScore());
    double totalScore =
        evaluation.totalScore() != null
            ? defaultScore(evaluation.totalScore())
            : (correctnessScore + depthScore + practicalScore) / 3.0;

    return new EvaluationResultDto(
        correctnessScore,
        depthScore,
        practicalScore,
        totalScore,
        defaultScore(evaluation.confidence()),
        evaluation.knowledgeGaps() != null ? evaluation.knowledgeGaps() : List.of(),
        evaluation.strengths() != null ? evaluation.strengths() : List.of(),
        Boolean.TRUE.equals(evaluation.shouldAskFollowUp()),
        evaluation.followUpFocus(),
        defaultString(evaluation.candidateFeedback()),
        false);
  }

  private double defaultScore(Double score) {
    return score != null && Double.isFinite(score) ? score : 0.0;
  }
}
