package io.github.kstnnn.user.service.dto;

import io.github.kstnnn.user.service.entity.User;
import io.github.kstnnn.user.service.enums.UserRole;
import io.github.kstnnn.user.service.enums.UserStatus;
import io.github.kstnnn.user.service.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record UserCreateRequestDto(
    @NotBlank(message = "Provider id is required.") String providerUserId,
    @NotBlank(message = "Email is required.") @Email(message = "Email must be valid.") String email,
    @NotNull(message = "User type is required.") UserType userType,
    @NotBlank(message = "First name is required.") String firstName,
    String lastName,
    @NotEmpty(message = "At least one role is required.") Set<UserRole> roles) {
  public User toEntity() {
    return User.builder()
        .providerUserId(providerUserId)
        .email(email)
        .userType(userType)
        .userStatus(UserStatus.ACTIVE)
        .firstName(firstName)
        .lastName(lastName)
        .emailVerified(false)
        .roles(roles)
        .build();
  }
}
