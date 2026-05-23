package io.github.kstnnn.organization.service.service.impl;

import io.github.kstnnn.organization.service.dto.AiSessionCreatedResponse;
import io.github.kstnnn.organization.service.dto.AiStartInterviewRequest;
import io.github.kstnnn.organization.service.service.AiInterviewClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AiInterviewClientImpl implements AiInterviewClient {

  private final RestClient restClient;

  public AiInterviewClientImpl(
      RestClient.Builder restClientBuilder,
      @Value("${app.ai-interview-service.base-url}") String aiInterviewServiceBaseUrl) {
    this.restClient = restClientBuilder.baseUrl(aiInterviewServiceBaseUrl).build();
  }

  @Override
  public AiSessionCreatedResponse createSession(AiStartInterviewRequest request) {
    return restClient.post().uri("/api/v1/interviews").body(request).retrieve().body(AiSessionCreatedResponse.class);
  }
}
