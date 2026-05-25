package io.github.kstnnn.ai.interview.service.dto;

import java.util.List;

public record TechnologyGroupResponseDto(
    String groupKey, String groupName, List<TechnologyResponseDto> items) {}
