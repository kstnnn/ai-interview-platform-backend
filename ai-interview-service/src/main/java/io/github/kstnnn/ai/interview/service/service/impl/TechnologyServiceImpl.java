package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.dto.TechnologyGroupResponseDto;
import io.github.kstnnn.ai.interview.service.dto.TechnologyResponseDto;
import io.github.kstnnn.ai.interview.service.repository.TechnologyRepository;
import io.github.kstnnn.ai.interview.service.service.TechnologyService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TechnologyServiceImpl implements TechnologyService {

  private final TechnologyRepository technologyRepository;

  @Override
  @Transactional(readOnly = true)
  public List<TechnologyResponseDto> getActiveTechnologies() {
    return activeTechnologyDtos();
  }

  @Override
  @Transactional(readOnly = true)
  public List<TechnologyGroupResponseDto> getGroupedActiveTechnologies() {
    return activeTechnologyDtos().stream()
        .collect(
            Collectors.groupingBy(
                TechnologyResponseDto::groupKey,
                LinkedHashMap::new,
                Collectors.toList()))
        .entrySet()
        .stream()
        .map(
            entry ->
                new TechnologyGroupResponseDto(
                    entry.getKey(), entry.getValue().get(0).groupName(), entry.getValue()))
        .toList();
  }

  private List<TechnologyResponseDto> activeTechnologyDtos() {
    return technologyRepository.findByActiveTrueOrderBySortOrderAscDisplayNameAsc().stream()
        .map(TechnologyResponseDto::toDto)
        .toList();
  }
}
