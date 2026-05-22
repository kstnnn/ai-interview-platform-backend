package io.github.kstnnn.ai.interview.service.dto;

import java.math.BigDecimal;

public record TopicStateSummaryDto(
    String topic,
    int questionsAsked,
    BigDecimal masteryScore,
    BigDecimal confidenceScore,
    BigDecimal avgScore) {}
