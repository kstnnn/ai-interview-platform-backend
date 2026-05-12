package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.model.Difficulty;
import io.github.kstnnn.ai.interview.service.model.Question;
import io.github.kstnnn.ai.interview.service.model.Technology;
import io.github.kstnnn.ai.interview.service.repository.QuestionRepository;
import io.github.kstnnn.ai.interview.service.service.QuestionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

  private final VectorStore vectorStore;
  private final QuestionRepository qRepository;

  @Override
  public List<Question> getBaseQuestions(List<Technology> technologies) {
    var normalizedTechs =
        technologies == null
            ? List.<String>of()
            : technologies.stream()
                .filter(Objects::nonNull)
                .map(Technology::getKey)
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

    if (normalizedTechs.isEmpty()) {
      throw new IllegalArgumentException("Technologies must not be empty");
    }

    log.info("Base questions requested for technologies={}", normalizedTechs);

    List<Question> questions = new ArrayList<>();

    for (String technology : normalizedTechs) {
      questions.addAll(getQuestionsPerTechnology(technology));
    }

    return questions;
  }

  public List<Question> getQuestionsPerTechnology(String technology) {
    List<Question> questions = new ArrayList<>();
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

      var questionExternalIds =
          vectorStore.similaritySearch(searchRequest.build()).stream()
              .peek(
                  d ->
                      log.info(
                          "result: id={}, technology={}, difficulty={}",
                          d.getMetadata().get("id"),
                          d.getMetadata().get("technology"),
                          d.getMetadata().get("difficulty")))
              .map(d -> (String) d.getMetadata().get("id"))
              .toList();

      questions.addAll(qRepository.findAllByExternalIdIn(questionExternalIds));
    }

    return questions;
  }
}
