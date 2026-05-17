package io.github.kstnnn.ai.interview.service.dto;

import io.github.kstnnn.ai.interview.service.model.InterviewLevel;
import java.util.List;

public record GreetingDto(
    String candidateName,
    List<String> technologies,
    InterviewLevel interviewLevel,
    String interviewLanguage) {}
