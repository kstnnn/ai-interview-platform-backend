package io.github.kstnnn.user.service.service;

import io.github.kstnnn.user.service.dto.UserCreateRequestDto;
import io.github.kstnnn.user.service.dto.UserResponseDto;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserService {
  UserResponseDto getById(UUID id);

  UserResponseDto create(@AuthenticationPrincipal Jwt jwt, UserCreateRequestDto dto);

  void deleteById(UUID id);
}
