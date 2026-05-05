package io.github.kstnnn.ai.interview.service.controller;

import io.github.kstnnn.ai.interview.service.service.InterviewSessionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sessions")
@Slf4j
public class InterviewSessionController {

  private final InterviewSessionService interviewSessionService;

  @GetMapping("/questions")
  public ResponseEntity<List<String>> getBaseQuestions(@RequestParam List<String> techs) {
    log.info(techs.toString());
    List<String> questions = interviewSessionService.getBaseQuestions(techs);
    return ResponseEntity.ok(questions);
  }
}
