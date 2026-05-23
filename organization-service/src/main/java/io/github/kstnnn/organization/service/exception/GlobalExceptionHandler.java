package io.github.kstnnn.organization.service.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
    var errors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid",
                    (first, second) -> first));
    return Map.of("message", "Validation failed", "errors", errors);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  Map<String, String> handleNotFound(ResourceNotFoundException ex) {
    return Map.of("message", ex.getMessage());
  }

  @ExceptionHandler(InvalidVacancyStatusTransitionException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  Map<String, String> handleInvalidTransition(InvalidVacancyStatusTransitionException ex) {
    return Map.of("message", ex.getMessage());
  }

  @ExceptionHandler(DuplicateApplicationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  Map<String, String> handleDuplicateApplication(DuplicateApplicationException ex) {
    return Map.of("message", ex.getMessage());
  }

  @ExceptionHandler({
    AccessDeniedException.class,
    BusinessUserRequiredException.class,
    CandidateUserRequiredException.class
  })
  @ResponseStatus(HttpStatus.FORBIDDEN)
  Map<String, String> handleForbidden(RuntimeException ex) {
    return Map.of("message", ex.getMessage());
  }
}
