package io.github.kstnnn.user.service.service.impl;

import io.github.kstnnn.user.service.dto.AdminUserResponseDto;
import io.github.kstnnn.user.service.exception.UserNotFoundException;
import io.github.kstnnn.user.service.model.UserRole;
import io.github.kstnnn.user.service.model.UserStatus;
import io.github.kstnnn.user.service.model.UserType;
import io.github.kstnnn.user.service.repository.UserRepository;
import io.github.kstnnn.user.service.service.AdminUserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<AdminUserResponseDto> listUsers(
      String search, UserType userType, UserStatus userStatus, UserRole role, Pageable pageable) {
    var normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();
    if (normalizedSearch == null) {
      return userRepository
          .findAdminUsers(userType, userStatus, role, pageable)
          .map(AdminUserResponseDto::toDto);
    }
    return userRepository
        .findAdminUsersBySearch(normalizedSearch, userType, userStatus, role, pageable)
        .map(AdminUserResponseDto::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public AdminUserResponseDto getUser(UUID userId) {
    return userRepository
        .findById(userId)
        .filter(user -> user.getUserStatus() != UserStatus.DELETED)
        .map(AdminUserResponseDto::toDto)
        .orElseThrow(() -> new UserNotFoundException(userId));
  }

  @Override
  @Transactional
  public AdminUserResponseDto blockUser(UUID userId) {
    var user =
        userRepository
            .findById(userId)
            .filter(existing -> existing.getUserStatus() != UserStatus.DELETED)
            .orElseThrow(() -> new UserNotFoundException(userId));
    user.setUserStatus(UserStatus.BLOCKED);
    return AdminUserResponseDto.toDto(userRepository.save(user));
  }

  @Override
  @Transactional
  public AdminUserResponseDto unblockUser(UUID userId) {
    var user =
        userRepository
            .findById(userId)
            .filter(existing -> existing.getUserStatus() != UserStatus.DELETED)
            .orElseThrow(() -> new UserNotFoundException(userId));
    user.setUserStatus(UserStatus.ACTIVE);
    return AdminUserResponseDto.toDto(userRepository.save(user));
  }
}
