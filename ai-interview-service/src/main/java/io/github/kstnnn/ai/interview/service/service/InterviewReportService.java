package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.dto.InterviewReportDto;
import java.util.UUID;

public interface InterviewReportService {

  InterviewReportDto getMockReport(UUID sessionId, UUID userId);
}
