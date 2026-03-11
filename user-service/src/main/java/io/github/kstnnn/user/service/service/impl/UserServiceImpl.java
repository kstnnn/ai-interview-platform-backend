package io.github.kstnnn.user.service.service.impl;

import io.github.kstnnn.user.service.dto.UserCreateRequestDto;
import io.github.kstnnn.user.service.dto.UserResponseDto;
import io.github.kstnnn.user.service.exception.UserAlreadyExistsException;
import io.github.kstnnn.user.service.exception.UserNotFoundException;
import io.github.kstnnn.user.service.repository.UserRepository;
import io.github.kstnnn.user.service.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final TransactionTemplate transactionTemplate;

  @Override
  public UserResponseDto create(Jwt jwt, UserCreateRequestDto dto) {
    String providerUserId = jwt.getSubject();

    var existing = userRepository.findUserByProviderUserId(providerUserId);
    if (existing.isPresent()) {
      throw new UserAlreadyExistsException(dto.email());
    }

    var newUser = dto.toEntity();
    newUser.setEmail(dto.email());
    newUser.setProviderUserId(providerUserId);

    return UserResponseDto.toDto(
        transactionTemplate.execute(
            status -> {
              return userRepository.save(newUser);
            }));
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
