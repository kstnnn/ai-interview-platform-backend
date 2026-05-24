package io.github.kstnnn.organization.service.dto;

import java.util.Set;
import java.util.UUID;

public record UserAuthLookupDto(UUID id, String userStatus, Set<String> roles) {}
