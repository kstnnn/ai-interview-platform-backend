package io.github.kstnnn.organization.service.service;

import io.github.kstnnn.organization.service.dto.EmployerApplicationReportDto;
import io.github.kstnnn.organization.service.dto.VacancyApplicationResponse;
import io.github.kstnnn.organization.service.dto.VacancyApplyRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

public interface VacancyApplicationService {

  VacancyApplicationResponse apply(Jwt jwt, UUID vacancyId, VacancyApplyRequest request);

  List<VacancyApplicationResponse> getMyApplications(Jwt jwt);

  VacancyApplicationResponse getMyApplication(Jwt jwt, UUID applicationId);

  List<VacancyApplicationResponse> getVacancyApplications(Jwt jwt, UUID vacancyId);

  EmployerApplicationReportDto getEmployerReport(Jwt jwt, UUID vacancyId, UUID applicationId);

  String exportVacancyApplicationsCsv(Jwt jwt, UUID vacancyId);

  byte[] exportEmployerReportPdf(Jwt jwt, UUID vacancyId, UUID applicationId, String language);
}
