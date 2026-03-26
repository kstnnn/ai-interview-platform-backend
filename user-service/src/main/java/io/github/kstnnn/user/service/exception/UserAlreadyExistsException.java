package io.github.kstnnn.user.service.exception;

public class UserAlreadyExistsException extends RuntimeException {
  private final String email;

  public UserAlreadyExistsException(String email) {
    super("User with email " + email + " already exists");
    this.email = email;
  }

  String getEmail() {
    return this.email;
  }
}
