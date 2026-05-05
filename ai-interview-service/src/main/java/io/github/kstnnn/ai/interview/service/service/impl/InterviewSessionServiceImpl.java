package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.model.Difficulty;
import io.github.kstnnn.ai.interview.service.service.InterviewSessionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter.Expression;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewSessionServiceImpl implements InterviewSessionService {

  private final ChatClient chatClient;
  private final VectorStore vectorStore;

  @Override
  public void startInterview(UUID userId, List<String> technologies) {
    throw new UnsupportedOperationException("Unimplemented method 'startInterview'");
  }

  @Override
  public List<String> getBaseQuestions(List<String> technologies) {
    var normalizedTechs =
        technologies == null
            ? List.<String>of()
            : technologies.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

    if (normalizedTechs.isEmpty()) {
      throw new IllegalArgumentException("technologies must not be empty");
    }

    log.info("Base questions requested for technologies={}", normalizedTechs);

    List<String> questions = new ArrayList<>();

    for (String technology : normalizedTechs) {
      questions.addAll(getQuestionsPerTechnology(technology));
    }

    return questions;
  }

  private List<String> getQuestionsPerTechnology(String technology) {
    List<String> questions = new ArrayList<>();
    var filterExpressionBuilder = new FilterExpressionBuilder();

    var searchRequest = SearchRequest.builder().similarityThreshold(0.7).topK(1);

    for (var difficulty : Difficulty.values()) {
      var expression =
          filterExpressionBuilder
              .and(
                  filterExpressionBuilder.eq("technology", technology),
                  filterExpressionBuilder.eq("difficulty", difficulty.toString()))
              .build();

      searchRequest.filterExpression(expression);

      questions.addAll(
          vectorStore.similaritySearch(searchRequest.build()).stream()
              .peek(
                  d ->
                      log.info(
                          "result: id={}, technology={}, difficulty={}",
                          d.getMetadata().get("id"),
                          d.getMetadata().get("technology"),
                          d.getMetadata().get("difficulty")))
              .map(Document::getText)
              .toList());
    }

    return questions;
  }

  private Expression buildExpression(List<String> technologies, Difficulty difficulty) {
    var fBuilder = new FilterExpressionBuilder();
    var expression = fBuilder.eq("technology", technologies.get(0));
    for (int i = 1; i < technologies.size(); i++) {
      expression = fBuilder.or(expression, fBuilder.eq("technology", technologies.get(i)));
    }
    return fBuilder.and(expression, fBuilder.eq("difficulty", difficulty.toString())).build();
  }
}
