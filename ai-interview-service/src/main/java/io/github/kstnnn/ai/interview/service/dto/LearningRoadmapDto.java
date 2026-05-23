package io.github.kstnnn.ai.interview.service.dto;

import java.util.List;
import java.util.UUID;

public record LearningRoadmapDto(
    UUID sessionId, String language, String summary, List<LearningRoadmapTopicDto> priorityTopics) {}
