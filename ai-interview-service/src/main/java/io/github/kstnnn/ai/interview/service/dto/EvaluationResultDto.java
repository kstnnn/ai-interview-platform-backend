package io.github.kstnnn.ai.interview.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvaluationResultDto(
    @JsonProperty("correctnessScore") int correctnessScore,
    @JsonProperty("depthScore") int depthScore,
    @JsonProperty("practicalScore") int practicalScore,
    @JsonProperty("totalScore") int totalScore,
    @JsonProperty("confidence") double confidence,
    @JsonProperty("knowledgeGaps") List<String> knowledgeGaps,
    @JsonProperty("strengths") List<String> strengths,
    @JsonProperty("shouldAskFollowUp") boolean shouldAskFollowUp,
    @JsonProperty("followUpFocus") String followUpFocus,
    @JsonProperty("candidateFeedback") String candidateFeedback) {}
