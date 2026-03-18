package io.github.kstnnn.user.service.exception;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<Map<String, String>> handleUserAlreadyExists(
      UserAlreadyExistsException ex) {

    log.error("User with email {} already exists", maskEmail(ex.getEmail()));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
  }

  private String maskEmail(String email) {
    int atIndex = email.indexOf('@');
    String regex = "(.{2})(.*)(@.*)";
    String repeatedAsterisks = "*".repeat(atIndex - 2);
    String maskedEmail = email.replaceAll(regex, "$1" + repeatedAsterisks + "$3");
    return maskedEmail;
  }
}
