package io.github.kstnnn.organization.service.dto;

import java.math.BigDecimal;

public record AiTopicStateSummaryDto(
    String topic,
    int questionsAsked,
    BigDecimal masteryScore,
    BigDecimal confidenceScore,
    BigDecimal avgScore) {}
