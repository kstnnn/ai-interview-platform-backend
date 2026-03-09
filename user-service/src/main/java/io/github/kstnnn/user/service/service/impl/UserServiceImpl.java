package io.github.kstnnn.user.service.service.impl;

import io.github.kstnnn.user.service.dto.UserCreateRequestDto;
import io.github.kstnnn.user.service.dto.UserResponseDto;
import io.github.kstnnn.user.service.exception.UserNotFoundException;
import io.github.kstnnn.user.service.repository.UserRepository;
import io.github.kstnnn.user.service.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public UserResponseDto create(UserCreateRequestDto newUser) {
    return UserResponseDto.toDto(userRepository.save(newUser.toEntity()));
  }

  @Override
  public void deleteById(UUID id) {
    userRepository.deleteById(id);
  }

  @Override
  public UserResponseDto getById(UUID id) {
    return userRepository.findResponseDtoById(id).orElseThrow(() -> new UserNotFoundException(id));
  }
}
