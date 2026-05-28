package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.dto.AdminQuestionCreateRequest;
import io.github.kstnnn.ai.interview.service.dto.AdminQuestionResponseDto;
import io.github.kstnnn.ai.interview.service.dto.AdminQuestionUpdateRequest;
import io.github.kstnnn.ai.interview.service.exception.TechnologyNotFoundException;
import io.github.kstnnn.ai.interview.service.model.Difficulty;
import io.github.kstnnn.ai.interview.service.model.Question;
import io.github.kstnnn.ai.interview.service.repository.QuestionRepository;
import io.github.kstnnn.ai.interview.service.repository.TechnologyRepository;
import io.github.kstnnn.ai.interview.service.service.AdminQuestionService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminQuestionServiceImpl implements AdminQuestionService {

  private final QuestionRepository questionRepository;
  private final TechnologyRepository technologyRepository;
  private final VectorStore vectorStore;

  @Override
  @Transactional(readOnly = true)
  public Page<AdminQuestionResponseDto> listQuestions(
      String search, String technologyKey, Difficulty difficulty, Boolean active, Pageable pageable) {
    var normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();
    var normalizedTechnologyKey =
        technologyKey == null || technologyKey.isBlank() ? null : technologyKey.trim().toLowerCase();
    if (normalizedSearch == null) {
      return questionRepository
          .findAdminQuestions(normalizedTechnologyKey, difficulty, active, pageable)
          .map(AdminQuestionResponseDto::toDto);
    }
    return questionRepository
        .findAdminQuestionsBySearch(normalizedSearch, normalizedTechnologyKey, difficulty, active, pageable)
        .map(AdminQuestionResponseDto::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AdminQuestionResponseDto> exportQuestions(
      String search, String technologyKey, Difficulty difficulty, Boolean active) {
    var normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();
    var normalizedTechnologyKey =
        technologyKey == null || technologyKey.isBlank() ? null : technologyKey.trim().toLowerCase();
    if (normalizedSearch == null) {
      return questionRepository
          .findAdminQuestionsForExport(normalizedTechnologyKey, difficulty, active)
          .stream()
          .map(AdminQuestionResponseDto::toDto)
          .toList();
    }
    return questionRepository
        .findAdminQuestionsBySearchForExport(normalizedSearch, normalizedTechnologyKey, difficulty, active)
        .stream()
        .map(AdminQuestionResponseDto::toDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public String exportQuestionsCsv(String search, String technologyKey, Difficulty difficulty, Boolean active) {
    var questions = exportQuestions(search, technologyKey, difficulty, active);
    var csv = new StringBuilder();
    csv.append('\ufeff');
    appendCsvRow(
        csv,
        List.of(
            "id",
            "external_id",
            "technology_key",
            "technology_name",
            "topic",
            "subtopic",
            "difficulty",
            "question_text",
            "expected_answer",
            "active",
            "created_at",
            "updated_at"));
    for (var question : questions) {
      appendCsvRow(
          csv,
          List.of(
              value(question.id()),
              value(question.externalId()),
              question.technology() != null ? value(question.technology().key()) : "",
              question.technology() != null ? value(question.technology().displayName()) : "",
              value(question.topic()),
              value(question.subtopic()),
              value(question.difficulty()),
              value(question.questionText()),
              value(question.expectedAnswer()),
              value(question.active()),
              value(question.createdAt()),
              value(question.updatedAt())));
    }
    return csv.toString();
  }

  @Override
  @Transactional(readOnly = true)
  public AdminQuestionResponseDto getQuestion(UUID questionId) {
    return questionRepository
        .findById(questionId)
        .map(AdminQuestionResponseDto::toDto)
        .orElseThrow(() -> new IllegalStateException("Question not found"));
  }

  @Override
  @Transactional
  public AdminQuestionResponseDto createQuestion(AdminQuestionCreateRequest request) {
    var externalId = trimToNull(request.externalId());
    if (externalId == null) {
      externalId = "admin-" + UUID.randomUUID();
    }
    questionRepository
        .findByExternalId(externalId)
        .ifPresent(
            existing -> {
              throw new IllegalStateException("Question externalId already exists");
            });

    var question = new Question();
    question.setExternalId(externalId);
    apply(question, request.technologyKey(), request.topic(), request.subtopic(), request.difficulty(), request.questionText(), request.expectedAnswer());
    question.setActive(request.active() == null || request.active());
    question.setUpdatedAt(Instant.now());
    var saved = questionRepository.save(question);
    syncVectorStore(saved);
    return AdminQuestionResponseDto.toDto(saved);
  }

  @Override
  @Transactional
  public AdminQuestionResponseDto updateQuestion(UUID questionId, AdminQuestionUpdateRequest request) {
    var question =
        questionRepository
            .findById(questionId)
            .orElseThrow(() -> new IllegalStateException("Question not found"));
    apply(question, request.technologyKey(), request.topic(), request.subtopic(), request.difficulty(), request.questionText(), request.expectedAnswer());
    if (request.active() != null) {
      question.setActive(request.active());
    }
    question.setUpdatedAt(Instant.now());
    var saved = questionRepository.save(question);
    syncVectorStore(saved);
    return AdminQuestionResponseDto.toDto(saved);
  }

  @Override
  @Transactional
  public AdminQuestionResponseDto activateQuestion(UUID questionId) {
    var question =
        questionRepository
            .findById(questionId)
            .orElseThrow(() -> new IllegalStateException("Question not found"));
    question.setActive(true);
    question.setUpdatedAt(Instant.now());
    var saved = questionRepository.save(question);
    syncVectorStore(saved);
    return AdminQuestionResponseDto.toDto(saved);
  }

  @Override
  @Transactional
  public AdminQuestionResponseDto deactivateQuestion(UUID questionId) {
    var question =
        questionRepository
            .findById(questionId)
            .orElseThrow(() -> new IllegalStateException("Question not found"));
    question.setActive(false);
    question.setUpdatedAt(Instant.now());
    var saved = questionRepository.save(question);
    vectorStore.delete(List.of(saved.getExternalId()));
    return AdminQuestionResponseDto.toDto(saved);
  }

  private void apply(
      Question question,
      String technologyKey,
      String topic,
      String subtopic,
      Difficulty difficulty,
      String questionText,
      String expectedAnswer) {
    var normalizedTechnologyKey = technologyKey.trim().toLowerCase();
    var technology =
        technologyRepository
            .findByKey(normalizedTechnologyKey)
            .orElseThrow(() -> new TechnologyNotFoundException(normalizedTechnologyKey));
    question.setTechnology(technology);
    question.setTopic(topic.trim());
    question.setSubtopic(trimToNull(subtopic));
    question.setDifficulty(difficulty);
    question.setQuestionText(questionText.trim());
    question.setExpectedAnswer(expectedAnswer.trim());
  }

  private void syncVectorStore(Question question) {
    vectorStore.delete(List.of(question.getExternalId()));
    if (!question.isActive()) {
      return;
    }
    vectorStore.add(
        List.of(
            new Document(
                question.getQuestionText(),
                Map.of(
                    "id", question.getExternalId(),
                    "technology", question.getTechnology().getKey(),
                    "topic", question.getTopic(),
                    "subtopic", question.getSubtopic() != null ? question.getSubtopic() : "",
                    "difficulty", question.getDifficulty().toString(),
                    "answer", question.getExpectedAnswer()))));
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    var trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private void appendCsvRow(StringBuilder csv, List<String> values) {
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        csv.append(',');
      }
      csv.append(csvEscape(values.get(i)));
    }
    csv.append('\n');
  }

  private String csvEscape(String value) {
    if (value == null) {
      return "";
    }
    return '"' + value.replace("\r", " ").replace("\n", " ").replace("\"", "\"\"") + '"';
  }

  private String value(Object value) {
    return value == null ? "" : value.toString();
  }
}
