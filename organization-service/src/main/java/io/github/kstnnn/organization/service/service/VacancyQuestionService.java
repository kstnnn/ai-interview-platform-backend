package io.github.kstnnn.organization.service.service;

import io.github.kstnnn.organization.service.dto.VacancyQuestionRequest;
import io.github.kstnnn.organization.service.dto.VacancyQuestionResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

public interface VacancyQuestionService {

  VacancyQuestionResponse create(Jwt jwt, UUID vacancyId, VacancyQuestionRequest request);

  List<VacancyQuestionResponse> list(Jwt jwt, UUID vacancyId);

  VacancyQuestionResponse update(Jwt jwt, UUID vacancyId, UUID questionId, VacancyQuestionRequest request);

  void delete(Jwt jwt, UUID vacancyId, UUID questionId);
}
