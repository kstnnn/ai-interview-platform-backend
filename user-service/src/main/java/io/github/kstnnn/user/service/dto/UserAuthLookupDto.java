package io.github.kstnnn.user.service.dto;

import io.github.kstnnn.user.service.model.User;
import io.github.kstnnn.user.service.model.UserRole;
import io.github.kstnnn.user.service.model.UserStatus;
import java.util.Set;
import java.util.UUID;

public record UserAuthLookupDto(UUID id, UserStatus userStatus, Set<UserRole> roles) {
  public static UserAuthLookupDto toDto(User user) {
    return new UserAuthLookupDto(user.getId(), user.getUserStatus(), user.getRoles());
  }
}
