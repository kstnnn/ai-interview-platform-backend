package io.github.kstnnn.ai.interview.service.controller;

import io.github.kstnnn.ai.interview.service.service.QuestionImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionImportController {
  private final QuestionImportService questionImportService;

  @PostMapping("/import")
  public ResponseEntity<Void> importQuestionsToDb() {
    questionImportService.loadQuestionsIntoDb();
    return ResponseEntity.ok().build();
  }

  @PostMapping("/sync/vector")
  public ResponseEntity<Void> syncVectorStore() {
    questionImportService.loadQuestionsIntoVectorStore();
    return ResponseEntity.ok().build();
  }
}
