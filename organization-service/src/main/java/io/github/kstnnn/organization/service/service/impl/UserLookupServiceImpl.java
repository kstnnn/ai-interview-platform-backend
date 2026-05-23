package io.github.kstnnn.organization.service.service.impl;

import io.github.kstnnn.organization.service.dto.UserLookupResponse;
import io.github.kstnnn.organization.service.service.UserLookupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class UserLookupServiceImpl implements UserLookupService {

  private final RestClient restClient;

  public UserLookupServiceImpl(
      RestClient.Builder restClientBuilder,
      @Value("${app.user-service.base-url}") String userServiceBaseUrl) {
    this.restClient = restClientBuilder.baseUrl(userServiceBaseUrl).build();
  }

  @Override
  public UserLookupResponse resolveByProviderUserId(String providerUserId) {
    return restClient
        .get()
        .uri("/api/v1/users/by-provider-id/{providerUserId}", providerUserId)
        .retrieve()
        .body(UserLookupResponse.class);
  }
}
