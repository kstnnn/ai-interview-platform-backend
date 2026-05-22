package io.github.kstnnn.ai.interview.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AiEvaluationDto(
    @JsonProperty("correctnessScore") Double correctnessScore,
    @JsonProperty("depthScore") Double depthScore,
    @JsonProperty("practicalScore") Double practicalScore,
    @JsonProperty("totalScore") Double totalScore,
    @JsonProperty("confidence") Double confidence,
    @JsonProperty("knowledgeGaps") List<String> knowledgeGaps,
    @JsonProperty("strengths") List<String> strengths,
    @JsonProperty("shouldAskFollowUp") Boolean shouldAskFollowUp,
    @JsonProperty("followUpFocus") String followUpFocus,
    @JsonProperty("candidateFeedback") String candidateFeedback) {}
