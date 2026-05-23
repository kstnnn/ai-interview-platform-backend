package io.github.kstnnn.organization.service.exception;

public class CandidateUserRequiredException extends RuntimeException {

  public CandidateUserRequiredException() {
    super("Only active candidate users can apply to vacancies");
  }
}
