package io.github.kstnnn.user.service.controller;

import io.github.kstnnn.user.service.dto.AdminUserResponseDto;
import io.github.kstnnn.user.service.model.UserRole;
import io.github.kstnnn.user.service.model.UserStatus;
import io.github.kstnnn.user.service.model.UserType;
import io.github.kstnnn.user.service.service.AdminUserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

  private final AdminUserService adminUserService;

  @GetMapping
  public Page<AdminUserResponseDto> listUsers(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) UserType userType,
      @RequestParam(required = false) UserStatus userStatus,
      @RequestParam(required = false) UserRole role,
      Pageable pageable) {
    return adminUserService.listUsers(search, userType, userStatus, role, pageable);
  }

  @GetMapping("/{userId}")
  public AdminUserResponseDto getUser(@PathVariable UUID userId) {
    return adminUserService.getUser(userId);
  }

  @PostMapping("/{userId}/block")
  public AdminUserResponseDto blockUser(@PathVariable UUID userId) {
    return adminUserService.blockUser(userId);
  }

  @PostMapping("/{userId}/unblock")
  public AdminUserResponseDto unblockUser(@PathVariable UUID userId) {
    return adminUserService.unblockUser(userId);
  }
}
