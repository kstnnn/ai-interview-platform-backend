package io.github.kstnnn.user.service.dto;

import io.github.kstnnn.user.service.entity.User;
import io.github.kstnnn.user.service.enums.UserStatus;
import io.github.kstnnn.user.service.enums.UserType;

public record UserCreateRequestDto(
    String providerUserId, String email, UserType userType, String firstName, String lastName) {
  public User toEntity() {
    return User.builder()
        .providerUserId(providerUserId)
        .email(email)
        .userType(userType)
        .userStatus(UserStatus.ACTIVE)
        .firstName(firstName)
        .lastName(lastName)
        .build();
  }
}
