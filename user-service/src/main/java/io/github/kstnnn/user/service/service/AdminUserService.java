package io.github.kstnnn.user.service.service;

import io.github.kstnnn.user.service.dto.AdminUserResponseDto;
import io.github.kstnnn.user.service.model.UserRole;
import io.github.kstnnn.user.service.model.UserStatus;
import io.github.kstnnn.user.service.model.UserType;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {
  Page<AdminUserResponseDto> listUsers(
      String search, UserType userType, UserStatus userStatus, UserRole role, Pageable pageable);

  AdminUserResponseDto getUser(UUID userId);

  AdminUserResponseDto blockUser(UUID userId);

  AdminUserResponseDto unblockUser(UUID userId);
}
