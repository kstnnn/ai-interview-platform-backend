package io.github.kstnnn.organization.service.service;

import io.github.kstnnn.organization.service.dto.OrganizationCreateRequest;
import io.github.kstnnn.organization.service.dto.OrganizationResponse;
import io.github.kstnnn.organization.service.dto.OrganizationUpdateRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

public interface OrganizationService {

  OrganizationResponse create(Jwt jwt, OrganizationCreateRequest request);

  List<OrganizationResponse> getMyOrganizations(Jwt jwt);

  OrganizationResponse getById(Jwt jwt, UUID organizationId);

  OrganizationResponse update(Jwt jwt, UUID organizationId, OrganizationUpdateRequest request);
}
