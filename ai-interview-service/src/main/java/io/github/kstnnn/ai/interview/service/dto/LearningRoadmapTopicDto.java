package io.github.kstnnn.ai.interview.service.dto;

import java.util.List;

public record LearningRoadmapTopicDto(
    String topic,
    double score,
    String reason,
    List<String> recommendedActions,
    List<LearningResourceDto> resources) {}
