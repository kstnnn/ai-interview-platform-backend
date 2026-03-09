package io.github.kstnnn.user.service.dto;

import io.github.kstnnn.user.service.enums.UserType;

public record UserCreateRequest(
    String providerUserId, String email, UserType userType, String firstName, String lastName) {}
