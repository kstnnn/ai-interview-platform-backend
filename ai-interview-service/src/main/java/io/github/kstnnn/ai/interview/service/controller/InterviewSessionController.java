package io.github.kstnnn.ai.interview.service.controller;

import io.github.kstnnn.ai.interview.service.service.InterviewSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sessions")
@Slf4j
public class InterviewSessionController {

  private final InterviewSessionService interviewSessionService;
}
