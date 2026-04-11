package io.github.kstnnn.user.service.exception;

import static io.github.kstnnn.common.util.MaskingUtils.mask;

import lombok.Getter;

@Getter
public class UserAlreadyExistsException extends RuntimeException {
  private final String field;
  private final String value;

  public UserAlreadyExistsException(String field, String value) {
    super(String.format("User with %s '%s' already exists", field, mask(value)));
    this.value = value;
    this.field = field;
  }
}
