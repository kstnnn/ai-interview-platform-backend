package io.github.kstnnn.ai.interview.service.dto;

import java.util.Set;
import java.util.UUID;

public record UserAuthLookupDto(UUID id, String userStatus, Set<String> roles) {}
