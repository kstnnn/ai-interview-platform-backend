package io.github.kstnnn.organization.service.exception;

public class DuplicateApplicationException extends RuntimeException {

  public DuplicateApplicationException() {
    super("Candidate already applied to this vacancy");
  }
}
