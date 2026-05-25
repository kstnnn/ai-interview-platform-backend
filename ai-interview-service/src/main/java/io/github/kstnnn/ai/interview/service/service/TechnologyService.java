package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.dto.TechnologyResponseDto;
import io.github.kstnnn.ai.interview.service.dto.TechnologyGroupResponseDto;
import java.util.List;

public interface TechnologyService {
  List<TechnologyResponseDto> getActiveTechnologies();

  List<TechnologyGroupResponseDto> getGroupedActiveTechnologies();
}
