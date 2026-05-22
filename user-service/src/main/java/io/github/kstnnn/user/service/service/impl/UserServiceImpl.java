package io.github.kstnnn.user.service.service.impl;

import io.github.kstnnn.user.service.dto.UserCreateRequestDto;
import io.github.kstnnn.user.service.dto.UserResponseDto;
import io.github.kstnnn.user.service.exception.UserAlreadyDeletedException;
import io.github.kstnnn.user.service.exception.UserAlreadyExistsException;
import io.github.kstnnn.user.service.exception.UserNotFoundException;
import io.github.kstnnn.user.service.model.UserStatus;
import io.github.kstnnn.user.service.repository.UserRepository;
import io.github.kstnnn.user.service.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public UserResponseDto create(UserCreateRequestDto dto) {
    log.info("User sign up attempt : {}", dto);
    log.info("Checking if the user already exists");

    if (userRepository.existsByEmail(dto.email())) {
      throw new UserAlreadyExistsException("email", dto.email());
    }

    if (userRepository.existsByProviderUserId(dto.providerUserId())) {
      throw new UserAlreadyExistsException("providerUserId", dto.providerUserId());
    }

    log.info("Saving new user");
    var newUser = userRepository.save(dto.toEntity());

    return UserResponseDto.toDto(newUser);
  }

  @Override
  public UserResponseDto getByProviderUserId(String providerUserId) {
    log.info("Get user by providerUserId {}", providerUserId);
    return userRepository
        .findResponseDtoByProviderUserId(providerUserId)
        .orElseThrow(() -> new UserNotFoundException("providerUserId", providerUserId));
  }

  @Override
  @Transactional
  public void deleteById(UUID id) {
    log.info("Check if user exists");

    var existing = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

    log.info("Delete user with id {} attempt.", id);

    if (existing.getUserStatus() == UserStatus.DELETED) {
      log.warn("User with id {} is already deleted", id);
      throw new UserAlreadyDeletedException(id);
    }

    existing.setUserStatus(UserStatus.DELETED);
    existing.setEmail("deleted_" + existing.getEmail());
    existing.setProviderUserId("deleted_" + existing.getProviderUserId());

    userRepository.save(existing);

    log.info("User has been deleted.");
  }

  @Override
  public UserResponseDto getById(UUID id) {
    log.info("Get user by id {} attempt", id);
    return userRepository.findResponseDtoById(id).orElseThrow(() -> new UserNotFoundException(id));
  }
}
