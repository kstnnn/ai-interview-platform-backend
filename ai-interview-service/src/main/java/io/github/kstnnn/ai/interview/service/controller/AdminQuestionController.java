package io.github.kstnnn.ai.interview.service.controller;

import io.github.kstnnn.ai.interview.service.dto.AdminQuestionCreateRequest;
import io.github.kstnnn.ai.interview.service.dto.AdminQuestionResponseDto;
import io.github.kstnnn.ai.interview.service.dto.AdminQuestionUpdateRequest;
import io.github.kstnnn.ai.interview.service.model.Difficulty;
import io.github.kstnnn.ai.interview.service.service.AdminQuestionService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/questions")
@RequiredArgsConstructor
public class AdminQuestionController {

  private final AdminQuestionService adminQuestionService;

  @GetMapping
  public Page<AdminQuestionResponseDto> listQuestions(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String technologyKey,
      @RequestParam(required = false) Difficulty difficulty,
      @RequestParam(required = false) Boolean active,
      Pageable pageable) {
    return adminQuestionService.listQuestions(search, technologyKey, difficulty, active, pageable);
  }

  @GetMapping("/{questionId}")
  public AdminQuestionResponseDto getQuestion(@PathVariable UUID questionId) {
    return adminQuestionService.getQuestion(questionId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AdminQuestionResponseDto createQuestion(@Valid @RequestBody AdminQuestionCreateRequest request) {
    return adminQuestionService.createQuestion(request);
  }

  @PatchMapping("/{questionId}")
  public AdminQuestionResponseDto updateQuestion(
      @PathVariable UUID questionId, @Valid @RequestBody AdminQuestionUpdateRequest request) {
    return adminQuestionService.updateQuestion(questionId, request);
  }

  @PostMapping("/{questionId}/activate")
  public AdminQuestionResponseDto activateQuestion(@PathVariable UUID questionId) {
    return adminQuestionService.activateQuestion(questionId);
  }

  @PostMapping("/{questionId}/deactivate")
  public AdminQuestionResponseDto deactivateQuestion(@PathVariable UUID questionId) {
    return adminQuestionService.deactivateQuestion(questionId);
  }
}
