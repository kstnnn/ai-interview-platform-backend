package io.github.kstnnn.ai.interview.service.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserLearningRoadmapDto(
    UUID userId,
    String language,
    Instant updatedAt,
    List<UUID> sourceSessionIds,
    String summary,
    List<UserLearningRoadmapTopicDto> priorityTopics) {}
