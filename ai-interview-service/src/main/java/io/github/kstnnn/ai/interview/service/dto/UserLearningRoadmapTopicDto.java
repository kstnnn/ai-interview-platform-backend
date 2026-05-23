package io.github.kstnnn.ai.interview.service.dto;

import java.util.List;

public record UserLearningRoadmapTopicDto(
    String topic,
    double currentScore,
    Double previousScore,
    String trend,
    String priority,
    String reason,
    List<String> recommendedActions,
    List<LearningResourceDto> resources) {}
