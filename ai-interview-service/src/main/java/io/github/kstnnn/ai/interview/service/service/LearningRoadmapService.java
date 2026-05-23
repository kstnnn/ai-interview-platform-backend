package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.dto.LearningRoadmapDto;
import io.github.kstnnn.ai.interview.service.dto.UserLearningRoadmapDto;
import java.util.UUID;

public interface LearningRoadmapService {

  LearningRoadmapDto getRoadmap(UUID sessionId, UUID userId, String language);

  UserLearningRoadmapDto getUserRoadmap(UUID userId, String language);
}
