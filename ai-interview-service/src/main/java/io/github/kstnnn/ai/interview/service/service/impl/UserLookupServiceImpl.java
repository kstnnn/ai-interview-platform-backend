package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.dto.UserLookupDto;
import io.github.kstnnn.ai.interview.service.service.UserLookupService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class UserLookupServiceImpl implements UserLookupService {

  private final RestClient restClient;

  public UserLookupServiceImpl(
      RestClient.Builder restClientBuilder,
      @Value("${app.user-service.base-url:http://localhost:8080}") String userServiceBaseUrl) {
    this.restClient = restClientBuilder.baseUrl(userServiceBaseUrl).build();
  }

  @Override
  public UUID resolveInternalUserId(String providerUserId) {
    var user =
        restClient
            .get()
            .uri("/api/v1/users/by-provider-id/{providerUserId}", providerUserId)
            .retrieve()
            .body(UserLookupDto.class);
    if (user == null || user.id() == null) {
      throw new IllegalStateException("User-service returned no user id");
    }
    return user.id();
  }
}
