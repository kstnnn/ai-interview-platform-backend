package io.github.kstnnn.ai.interview.service.dto;

import io.github.kstnnn.ai.interview.service.model.Technology;

public record TechnologyResponseDto(
    String key, String displayName, String groupKey, String groupName, int sortOrder) {
  public static TechnologyResponseDto toDto(Technology technology) {
    return new TechnologyResponseDto(
        technology.getKey(),
        technology.getDisplayName(),
        technology.getGroupKey(),
        technology.getGroupName(),
        technology.getSortOrder());
  }
}
