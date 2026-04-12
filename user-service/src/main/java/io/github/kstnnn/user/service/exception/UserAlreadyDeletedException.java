package io.github.kstnnn.user.service.exception;

import java.util.UUID;

public class UserAlreadyDeletedException extends RuntimeException {
  public UserAlreadyDeletedException(UUID id) {
    super(String.format("User with id %s is already deleted.", id));
  }
}
