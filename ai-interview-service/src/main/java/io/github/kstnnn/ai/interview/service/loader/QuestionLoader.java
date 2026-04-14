package io.github.kstnnn.ai.interview.service.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionLoader implements ApplicationRunner {

  private final VectorStore vectorStore;
  private final ObjectMapper objectMapper;

  @Override
  public void run(ApplicationArguments args) {
    List<Document> existing =
        vectorStore.similaritySearch(SearchRequest.builder().query("test").topK(1).build());
    if (!existing.isEmpty()) {
      log.info("VectorStore already contains documents. Skipping question import.");
      return;
    }

    try {
      PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
      Resource[] resources = resolver.getResources("classpath:questions/*-questions.json");

      if (resources.length == 0) {
        log.warn("No question files found in classpath:questions/");
        return;
      }

      List<Document> allDocuments = new ArrayList<>();

      for (Resource resource : resources) {
        log.info("Loading questions from: {}", resource.getFilename());
        List<Map<String, Object>> questions = parseQuestions(resource);
        List<Document> documents = convertToDocuments(questions);
        allDocuments.addAll(documents);
      }

      if (!allDocuments.isEmpty()) {
        vectorStore.add(allDocuments);
        log.info("Loaded {} questions into VectorStore", allDocuments.size());
      }
    } catch (Exception e) {
      log.error("Failed to load questions", e);
    }
  }

  private List<Map<String, Object>> parseQuestions(Resource resource) throws IOException {
    try (InputStream is = resource.getInputStream()) {
      return objectMapper.readValue(is, new TypeReference<>() {});
    }
  }

  private List<Document> convertToDocuments(List<Map<String, Object>> questions) {
    return questions.stream()
        .map(
            q ->
                new Document(
                    (String) q.get("question"),
                    Map.of(
                        "id", q.get("id"),
                        "topic", q.get("topic"),
                        "subtopic", q.getOrDefault("subtopic", ""),
                        "difficulty", q.get("difficulty"),
                        "answer", q.get("answer"))))
        .toList();
  }
}
