package io.github.kstnnn.user.service.dto;

import io.github.kstnnn.user.service.model.User;
import io.github.kstnnn.user.service.model.UserRole;
import io.github.kstnnn.user.service.model.UserStatus;
import io.github.kstnnn.user.service.model.UserType;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AdminUserResponseDto(
    UUID id,
    String email,
    String firstName,
    String lastName,
    boolean emailVerified,
    UserType userType,
    UserStatus userStatus,
    Set<UserRole> roles,
    Instant createdAt) {
  public static AdminUserResponseDto toDto(User user) {
    return new AdminUserResponseDto(
        user.getId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.isEmailVerified(),
        user.getUserType(),
        user.getUserStatus(),
        user.getRoles(),
        user.getCreatedAt());
  }
}
