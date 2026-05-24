package io.github.kstnnn.organization.service.service.impl;

import io.github.kstnnn.organization.service.dto.AiInterviewReportDto;
import io.github.kstnnn.organization.service.dto.AiSessionCreatedResponse;
import io.github.kstnnn.organization.service.dto.AiStartInterviewRequest;
import io.github.kstnnn.organization.service.service.AiInterviewClient;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AiInterviewClientImpl implements AiInterviewClient {

  private final RestClient restClient;
  private final String internalToken;

  public AiInterviewClientImpl(
      RestClient.Builder restClientBuilder,
      @Value("${app.ai-interview-service.base-url}") String aiInterviewServiceBaseUrl,
      @Value("${app.internal-token}") String internalToken) {
    this.restClient = restClientBuilder.baseUrl(aiInterviewServiceBaseUrl).build();
    this.internalToken = internalToken;
  }

  @Override
  public AiSessionCreatedResponse createSession(AiStartInterviewRequest request) {
    return restClient.post().uri("/api/v1/interviews").body(request).retrieve().body(AiSessionCreatedResponse.class);
  }

  @Override
  public AiInterviewReportDto getReport(UUID sessionId) {
    return restClient
        .get()
        .uri("/api/v1/internal/interviews/{sessionId}/report", sessionId)
        .header("X-Internal-Token", internalToken)
        .retrieve()
        .body(AiInterviewReportDto.class);
  }
}
