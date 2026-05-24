package io.github.kstnnn.user.service.exception;

public class InvalidUserRegistrationException extends RuntimeException {
  public InvalidUserRegistrationException(String message) {
    super(message);
  }
}
