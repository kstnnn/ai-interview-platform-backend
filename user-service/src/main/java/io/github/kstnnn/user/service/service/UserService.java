package io.github.kstnnn.user.service.service;

import io.github.kstnnn.user.service.dto.UserAuthLookupDto;
import io.github.kstnnn.user.service.dto.UserCreateRequestDto;
import io.github.kstnnn.user.service.dto.UserResponseDto;
import java.util.UUID;

public interface UserService {
  UserResponseDto getById(UUID id);

  UserResponseDto getByProviderUserId(String providerUserId);

  UserAuthLookupDto getAuthByProviderUserId(String providerUserId);

  UserResponseDto create(UserCreateRequestDto dto);

  void deleteById(UUID id);
}
