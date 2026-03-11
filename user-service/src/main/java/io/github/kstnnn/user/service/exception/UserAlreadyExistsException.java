package io.github.kstnnn.user.service.exception;

public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException(String email) {
    super("User with email " + email + " already exists");
  }
}
