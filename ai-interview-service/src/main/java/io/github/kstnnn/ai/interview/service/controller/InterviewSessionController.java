package io.github.kstnnn.ai.interview.service.controller;

import io.github.kstnnn.ai.interview.service.dto.GreetingDto;
import io.github.kstnnn.ai.interview.service.service.AiService;
import io.github.kstnnn.ai.interview.service.service.InterviewSessionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
@Slf4j
public class InterviewSessionController {

  private final InterviewSessionService iSessionService;
  private final AiService aService;

  @PostMapping("{id}/start")
  public ResponseEntity<List<String>> greeting(
      @PathVariable UUID sessionId, @RequestBody GreetingDto dto) {
    List<String> responses = new ArrayList<>();
    iSessionService.startSession(sessionId);
    responses.add(aService.greeting(dto));

    return ResponseEntity.ok(responses);
  }
}
