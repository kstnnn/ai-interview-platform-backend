package io.github.kstnnn.organization.service.exception;

public class BusinessUserRequiredException extends RuntimeException {

  public BusinessUserRequiredException() {
    super("Only active business users can perform this action");
  }
}
