package io.github.kstnnn.user.service.service.impl;

import io.github.kstnnn.user.service.dto.UserCreateRequestDto;
import io.github.kstnnn.user.service.dto.UserResponseDto;
import io.github.kstnnn.user.service.exception.UserAlreadyExistsException;
import io.github.kstnnn.user.service.exception.UserNotFoundException;
import io.github.kstnnn.user.service.repository.UserRepository;
import io.github.kstnnn.user.service.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public UserResponseDto create(UserCreateRequestDto dto) {
    log.info("User sign up attempt : {}", dto);
    log.info("Checking if the user already exists");
    var existing = userRepository.findUserByProviderUserId(dto.providerUserId());
    if (existing.isPresent()) {
      throw new UserAlreadyExistsException(dto.email());
    }

    log.info("Saving new user");
    var newUser = userRepository.save(dto.toEntity());

    return UserResponseDto.toDto(newUser);
  }

  @Override
  public void deleteById(UUID id) {
    log.info("Delete user with id {} attempt", id);
    userRepository.deleteById(id);
  }

  @Override
  public UserResponseDto getById(UUID id) {
    log.info("Get user by id {} attempt", id);
    return userRepository.findResponseDtoById(id).orElseThrow(() -> new UserNotFoundException(id));
  }
}
