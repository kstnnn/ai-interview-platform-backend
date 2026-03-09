package io.github.kstnnn.user.service.service;

import io.github.kstnnn.user.service.dto.UserCreateRequestDto;
import io.github.kstnnn.user.service.dto.UserResponseDto;
import java.util.UUID;

public interface UserService {
  UserResponseDto getById(UUID id);

  UserResponseDto create(UserCreateRequestDto newUser);

  void deleteById(UUID id);
}
