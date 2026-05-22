package io.github.kstnnn.user.service.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException(UUID id) {
    super(String.format("User not found with id %s", id));
  }

  public UserNotFoundException(String field, String value) {
    super(String.format("User not found with %s %s", field, value));
  }
}
