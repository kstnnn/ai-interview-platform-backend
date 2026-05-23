package io.github.kstnnn.organization.service.exception;

import io.github.kstnnn.organization.service.model.VacancyStatus;

public class InvalidVacancyStatusTransitionException extends RuntimeException {

  public InvalidVacancyStatusTransitionException(VacancyStatus currentStatus, VacancyStatus targetStatus) {
    super("Cannot change vacancy status from " + currentStatus + " to " + targetStatus);
  }
}
