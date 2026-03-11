package io.github.kstnnn.user.service.dto;

import io.github.kstnnn.user.service.entity.User;
import io.github.kstnnn.user.service.enums.UserRole;
import io.github.kstnnn.user.service.enums.UserStatus;
import io.github.kstnnn.user.service.enums.UserType;
import java.util.Set;

public record UserCreateRequestDto(
    String email, UserType userType, String firstName, String lastName, Set<UserRole> roles) {
  public User toEntity() {
    return User.builder()
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
