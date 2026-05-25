package io.github.kstnnn.ai.interview.service.controller;

import io.github.kstnnn.ai.interview.service.dto.TechnologyGroupResponseDto;
import io.github.kstnnn.ai.interview.service.dto.TechnologyResponseDto;
import io.github.kstnnn.ai.interview.service.service.TechnologyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/technologies")
@RequiredArgsConstructor
public class TechnologyController {

  private final TechnologyService technologyService;

  @GetMapping
  public List<TechnologyResponseDto> getTechnologies() {
    return technologyService.getActiveTechnologies();
  }

  @GetMapping("/grouped")
  public List<TechnologyGroupResponseDto> getGroupedTechnologies() {
    return technologyService.getGroupedActiveTechnologies();
  }
}
