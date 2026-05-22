package io.github.kstnnn.ai.interview.service.controller;

import io.github.kstnnn.ai.interview.service.model.Question;
import io.github.kstnnn.ai.interview.service.service.QuestionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Slf4j
public class QuestionController {

  private final QuestionService questionService;

  @GetMapping
  public ResponseEntity<List<Question>> getBaseQuestions(@RequestParam List<String> technologyKeys) {
    log.info("technologyKeys={}", technologyKeys);
    List<Question> questions = questionService.getBaseQuestions(technologyKeys);
    return ResponseEntity.ok(questions);
  }
}
