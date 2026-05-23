package io.github.kstnnn.organization.service.dto;

import io.github.kstnnn.organization.service.model.UserStatus;
import io.github.kstnnn.organization.service.model.UserType;
import java.util.UUID;

public record UserLookupResponse(UUID id, UserType userType, UserStatus userStatus) {}
