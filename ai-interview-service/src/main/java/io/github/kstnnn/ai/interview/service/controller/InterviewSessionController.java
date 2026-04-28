package io.github.kstnnn.ai.interview.service.controller;

import io.github.kstnnn.ai.interview.service.service.InterviewSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sessions")
public class InterviewSessionController {

  private InterviewSessionService interviewSessionService;

  // @PostMapping()
  // public String createInterviewSession(@RequestBody StartInterviewSessionDto dto) {}
}
