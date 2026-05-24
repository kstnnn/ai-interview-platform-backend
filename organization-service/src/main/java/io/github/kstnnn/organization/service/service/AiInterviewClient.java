package io.github.kstnnn.organization.service.service;

import io.github.kstnnn.organization.service.dto.AiSessionCreatedResponse;
import io.github.kstnnn.organization.service.dto.AiInterviewReportDto;
import io.github.kstnnn.organization.service.dto.AiStartInterviewRequest;
import java.util.UUID;

public interface AiInterviewClient {

  AiSessionCreatedResponse createSession(AiStartInterviewRequest request);

  AiInterviewReportDto getReport(UUID sessionId);
}
