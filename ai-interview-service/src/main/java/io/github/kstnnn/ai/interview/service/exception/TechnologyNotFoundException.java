package io.github.kstnnn.ai.interview.service.exception;

public class TechnologyNotFoundException extends RuntimeException {
  public TechnologyNotFoundException(String key) {
    super(String.format("Technology with key %s not found.", key));
  }
}
