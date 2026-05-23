package io.github.kstnnn.organization.service.service;

import io.github.kstnnn.organization.service.dto.AiSessionCreatedResponse;
import io.github.kstnnn.organization.service.dto.AiStartInterviewRequest;

public interface AiInterviewClient {

  AiSessionCreatedResponse createSession(AiStartInterviewRequest request);
}
