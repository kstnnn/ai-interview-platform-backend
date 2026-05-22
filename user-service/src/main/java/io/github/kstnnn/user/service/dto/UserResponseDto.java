package io.github.kstnnn.user.service.dto;

import io.github.kstnnn.user.service.model.User;
import io.github.kstnnn.user.service.model.UserStatus;
import io.github.kstnnn.user.service.model.UserType;
import java.time.Instant;
import java.util.UUID;

public record UserResponseDto(
    UUID id,
    String email,
    String firstName,
    String lastName,
    Boolean emailVerified,
    UserType userType,
    UserStatus userStatus,
    Instant createdAt) {
  public static UserResponseDto toDto(User entity) {
    return new UserResponseDto(
        entity.getId(),
        entity.getEmail(),
        entity.getFirstName(),
        entity.getLastName(),
        entity.isEmailVerified(),
        entity.getUserType(),
        entity.getUserStatus(),
        entity.getCreatedAt());
  }
}
