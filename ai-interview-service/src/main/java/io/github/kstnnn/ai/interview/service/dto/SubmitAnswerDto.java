package io.github.kstnnn.ai.interview.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SubmitAnswerDto(
    @NotNull UUID sessionQuestionId,
    @NotBlank(message = "Answer text is required") String answerText) {}
