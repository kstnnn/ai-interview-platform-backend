package io.github.kstnnn.user.service.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

import io.github.kstnnn.user.service.dto.UserCreateRequestDto;
import io.github.kstnnn.user.service.entity.User;
import io.github.kstnnn.user.service.enums.UserRole;
import io.github.kstnnn.user.service.enums.UserStatus;
import io.github.kstnnn.user.service.enums.UserType;
import io.github.kstnnn.user.service.exception.UserAlreadyExistsException;
import io.github.kstnnn.user.service.exception.UserNotFoundException;
import io.github.kstnnn.user.service.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserServiceImpl userServiceImpl;

  @Test
  void shouldCreateUserAndReturnDtoWhenUserDoesNotExist() {
    // Given
    var providerUserId = "1234567890";
    var id = UUID.randomUUID();
    var email = "johndoe@example.com";
    var firstName = "John";
    var lastName = "Doe";
    var roles = Set.of(UserRole.CANDIDATE);
    var createdAt = Instant.now();

    UserCreateRequestDto userCreateRequestDto =
        new UserCreateRequestDto(
            providerUserId, email, UserType.PERSONAL, firstName, lastName, roles);

    User savedUser =
        User.builder()
            .id(id)
            .email(email)
            .userType(UserType.PERSONAL)
            .firstName(firstName)
            .lastName(lastName)
            .providerUserId(providerUserId)
            .userStatus(UserStatus.ACTIVE)
            .roles(roles)
            .emailVerified(false)
            .createdAt(createdAt)
            .build();

    given(userRepository.save(any(User.class))).willReturn(savedUser);

    // When
    var result = userServiceImpl.create(userCreateRequestDto);

    // Then
    then(userRepository).should().save(any(User.class));

    assertEquals(id, result.id());
    assertEquals(email, result.email());
    assertEquals(firstName, result.firstName());
    assertEquals(lastName, result.lastName());
    assertEquals(false, result.emailVerified());
    assertEquals(UserType.PERSONAL, result.userType());
    assertEquals(UserStatus.ACTIVE, result.userStatus());
    assertEquals(createdAt, result.createdAt());
  }

  void shouldThrowUserAlreadyExistsExceptionWhenUserAlreadyExists() {
    // Given
    var providerUserId = "1234567890";
    var email = "johndoe@example.com";

    var dto =
        new UserCreateRequestDto(
            providerUserId, email, UserType.PERSONAL, "John", "Doe", Set.of(UserRole.CANDIDATE));

    var existingUser = User.builder().email(email).providerUserId(providerUserId).build();

    given(userRepository.findUserByProviderUserId(providerUserId))
        .willReturn(Optional.of(existingUser));

    // When + Then
    assertThrows(UserAlreadyExistsException.class, () -> userServiceImpl.create(dto));

    then(userRepository).should(never()).save(any());
  }

  void shouldReturnUserDtoWhenUserExists() {
    // Given
    var id = UUID.randomUUID();

    var existingUser = new User();

    given(userRepository.findById(id)).willReturn(Optional.of(existingUser));

    // When
    var result = userServiceImpl.getById(id);

    // Then
    assertEquals(id, result.id());
  }

  void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
    // Given
    var id = UUID.randomUUID();

    given(userRepository.findById(id)).willThrow(new UserNotFoundException(id));

    // When + Then
    assertThrows(UserNotFoundException.class, () -> userServiceImpl.getById(id));
  }

  void shouldDeleteUserWhenUserExists() {
    // Given
    var id = UUID.randomUUID();

    given(userRepository.existsById(id)).willReturn(true);

    // When
    userServiceImpl.deleteById(id);

    // Then
    then(userRepository).should().deleteById(id);
  }

  void shouldDeleteUserWhenUserDoesNotExist() {
    // Given
    var id = UUID.randomUUID();

    given(userRepository.existsById(id)).willReturn(false);

    // When + Then
    assertThrows(UserNotFoundException.class, () -> userServiceImpl.deleteById(id));

    then(userRepository).should(never()).deleteById(any());
  }
}
