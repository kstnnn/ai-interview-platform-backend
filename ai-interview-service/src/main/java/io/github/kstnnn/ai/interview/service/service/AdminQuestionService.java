package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.dto.AdminQuestionCreateRequest;
import io.github.kstnnn.ai.interview.service.dto.AdminQuestionResponseDto;
import io.github.kstnnn.ai.interview.service.dto.AdminQuestionUpdateRequest;
import io.github.kstnnn.ai.interview.service.model.Difficulty;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminQuestionService {
  Page<AdminQuestionResponseDto> listQuestions(
      String search, String technologyKey, Difficulty difficulty, Boolean active, Pageable pageable);

  List<AdminQuestionResponseDto> exportQuestions(
      String search, String technologyKey, Difficulty difficulty, Boolean active);

  String exportQuestionsCsv(String search, String technologyKey, Difficulty difficulty, Boolean active);

  AdminQuestionResponseDto getQuestion(UUID questionId);

  AdminQuestionResponseDto createQuestion(AdminQuestionCreateRequest request);

  AdminQuestionResponseDto updateQuestion(UUID questionId, AdminQuestionUpdateRequest request);

  AdminQuestionResponseDto activateQuestion(UUID questionId);

  AdminQuestionResponseDto deactivateQuestion(UUID questionId);
}
