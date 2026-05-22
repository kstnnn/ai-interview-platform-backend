package io.github.kstnnn.ai.interview.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvaluationResultDto(
    @JsonProperty("correctnessScore") double correctnessScore,
    @JsonProperty("depthScore") double depthScore,
    @JsonProperty("practicalScore") double practicalScore,
    @JsonProperty("totalScore") double totalScore,
    @JsonProperty("confidence") double confidence,
    @JsonProperty("knowledgeGaps") List<String> knowledgeGaps,
    @JsonProperty("strengths") List<String> strengths,
    @JsonProperty("shouldAskFollowUp") boolean shouldAskFollowUp,
    @JsonProperty("followUpFocus") String followUpFocus,
    @JsonProperty("candidateFeedback") String candidateFeedback,
    @JsonProperty("duplicateSubmission") boolean duplicateSubmission) {}
