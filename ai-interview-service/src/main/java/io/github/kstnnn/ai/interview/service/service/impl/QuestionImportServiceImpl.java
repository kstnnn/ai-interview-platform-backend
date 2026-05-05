package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.exception.TechnologyNotFoundException;
import io.github.kstnnn.ai.interview.service.model.Difficulty;
import io.github.kstnnn.ai.interview.service.model.Question;
import io.github.kstnnn.ai.interview.service.repository.QuestionRepository;
import io.github.kstnnn.ai.interview.service.repository.TechnologyRepository;
import io.github.kstnnn.ai.interview.service.service.QuestionImportService;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionImportServiceImpl implements QuestionImportService {

  private final QuestionRepository questionRepository;
  private final ObjectMapper objectMapper;
  private final TechnologyRepository technologyRepository;
  private final VectorStore vectorStore;
  private final ChromaApi chromaApi;

  @Override
  public void deleteCollection() {
    chromaApi.deleteCollection("default_tenant", "default_database", "interview-questions");
  }

  @Override
  @Transactional
  public void loadQuestionsIntoDb() {
    try {
      PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
      Resource[] resources = resolver.getResources("classpath:questions/*-questions.json");

      if (resources.length == 0) {
        log.warn("No question files found in classpath:questions/");
        return;
      }

      for (Resource resource : resources) {
        var filename = resource.getFilename();
        if (filename == null || filename.isBlank()) {
          log.warn("Skipping resource with empty filename: {}", resource);
          continue;
        }

        var technologyKey = resolveTechnologyKey(filename);
        log.info("Loading questions from: {} (techKey={})", filename, technologyKey);
        List<Map<String, Object>> jsonQuestions = parseQuestions(resource);
        List<Question> questions = mapToQuestionList(technologyKey, jsonQuestions, filename);
        questionRepository.saveAll(questions);
      }

    } catch (Exception e) {
      log.error("Failed to load questions", e);
      throw new IllegalStateException("Failed to import questions into database", e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public void loadQuestionsIntoVectorStore() {
    var questions = questionRepository.findAllWithTechnology();

    List<Document> documents = new ArrayList<>();

    for (var question : questions) {
      var document =
          new Document(
              question.getQuestionText(),
              Map.of(
                  "id", question.getExternalId(),
                  "technology", question.getTechnology().getKey(),
                  "topic", question.getTopic(),
                  "subtopic", question.getSubtopic(),
                  "difficulty", question.getDifficulty().toString(),
                  "answer", question.getExpectedAnswer()));
      documents.add(document);
    }
    var ids = questions.stream().map(Question::getExternalId).toList();
    vectorStore.delete(ids);

    vectorStore.add(documents);
    log.info("Loaded {} questions into VectorStore", documents.size());
  }

  private List<Map<String, Object>> parseQuestions(Resource resource) throws IOException {
    try (InputStream is = resource.getInputStream()) {
      return objectMapper.readValue(is, new TypeReference<>() {});
    }
  }

  private List<Question> mapToQuestionList(
      String key, List<Map<String, Object>> jsons, String filename) {
    var tech =
        technologyRepository.findByKey(key).orElseThrow(() -> new TechnologyNotFoundException(key));

    var externalIds =
        jsons.stream().map(j -> (String) j.get("id")).filter(Objects::nonNull).toList();

    var existingQuestions =
        questionRepository.findAllByExternalIdIn(externalIds).stream()
            .collect(Collectors.toMap(Question::getExternalId, q -> q));

    List<Question> questions = new ArrayList<>();

    for (var json : jsons) {
      var externalId = (String) json.get("id");

      if (!hasRequiredFields(json)) {
        log.warn("Skipping invalid question payload in {}: {}", filename, json);
        continue;
      }

      if (externalId == null || externalId.isBlank()) {
        log.warn("Question does not have external id in {}", filename);
        continue;
      }

      var difficulty = parseDifficulty((String) json.get("difficulty"), externalId, filename);

      var question = existingQuestions.getOrDefault(externalId, new Question());
      question.setExternalId(externalId);
      question.setTechnology(tech);
      question.setTopic((String) json.get("topic"));
      question.setSubtopic((String) json.getOrDefault("subtopic", ""));
      question.setDifficulty(difficulty);
      question.setQuestionText((String) json.get("question"));
      question.setExpectedAnswer((String) json.get("answer"));
      question.setActive(true);
      question.setUpdatedAt(Instant.now());

      questions.add(question);
    }
    return questions;
  }

  private String resolveTechnologyKey(String filename) {
    var baseKey = filename.replace("-questions.json", "");
    return baseKey.replace('-', '_');
  }

  private boolean hasRequiredFields(Map<String, Object> json) {
    return isNonBlank(json.get("id"))
        && isNonBlank(json.get("question"))
        && isNonBlank(json.get("answer"))
        && isNonBlank(json.get("topic"))
        && isNonBlank(json.get("difficulty"));
  }

  private boolean isNonBlank(Object value) {
    return value instanceof String s && !s.isBlank();
  }

  private Difficulty parseDifficulty(String value, String externalId, String filename) {
    try {
      return Difficulty.valueOf(value.toUpperCase());
    } catch (Exception e) {
      log.warn(
          "Skipping question {} from {} due to invalid difficulty: {}",
          externalId,
          filename,
          value);
      return Difficulty.MEDIUM;
    }
  }
}
