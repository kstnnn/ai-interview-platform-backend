package io.github.kstnnn.ai.interview.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VoiceSynthesisRequestDto(
    @NotBlank(message = "Text is required") @Size(max = 4000) String text,
    @Size(max = 64) String speaker) {}
