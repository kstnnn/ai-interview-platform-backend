package io.github.kstnnn.ai.interview.service.dto;

import io.github.kstnnn.ai.interview.service.model.Technology;

public record AdminTechnologyDto(Long id, String key, String displayName) {
  public static AdminTechnologyDto toDto(Technology technology) {
    return new AdminTechnologyDto(technology.getId(), technology.getKey(), technology.getDisplayName());
  }
}
