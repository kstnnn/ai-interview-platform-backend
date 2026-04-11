package io.github.kstnnn.user.service.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

import io.github.kstnnn.user.service.dto.UserCreateRequestDto;
import io.github.kstnnn.user.service.dto.UserResponseDto;
import io.github.kstnnn.user.service.entity.User;
import io.github.kstnnn.user.service.enums.UserRole;
import io.github.kstnnn.user.service.enums.UserStatus;
import io.github.kstnnn.user.service.enums.UserType;
import io.github.kstnnn.user.service.exception.UserAlreadyDeletedException;
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
  void shouldReturnUserResponseDtoWhenCreateUser() {
    // Given
    var providerUserId = "1234567890";
    var id = UUID.randomUUID();
    var email = "johndoe@example.com";
    var firstName = "John";
    var lastName = "Doe";
    var roles = Set.of(UserRole.CANDIDATE);
    var createdAt = Instant.now();
    var userCreateRequestDto =
        new UserCreateRequestDto(
            providerUserId, email, UserType.PERSONAL, firstName, lastName, roles);
    var savedUser =
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

  @Test
  void shouldThrowUserAlreadyExistsExceptionWhenCreateUserWithDuplicateEmail() {
    // Given
    var providerUserId = "1234567890";
    var email = "johndoe@example.com";
    var dto =
        new UserCreateRequestDto(
            providerUserId, email, UserType.PERSONAL, "John", "Doe", Set.of(UserRole.CANDIDATE));
    var isExists = true;

    given(userRepository.existsByEmail(email)).willReturn(isExists);

    // When + Then
    assertThrows(UserAlreadyExistsException.class, () -> userServiceImpl.create(dto));

    then(userRepository).should(never()).save(any(User.class));
  }

  @Test
  void shouldThrowUserAlreadyExistsExceptionWhenCreateUserWithDuplicateProviderUserId() {
    // Given
    var providerUserId = "1234567890";
    var email = "johndoe@example.com";
    var dto =
        new UserCreateRequestDto(
            providerUserId, email, UserType.PERSONAL, "John", "Doe", Set.of(UserRole.CANDIDATE));
    var isExists = true;

    given(userRepository.existsByProviderUserId(providerUserId)).willReturn(isExists);

    // When + Then
    assertThrows(UserAlreadyExistsException.class, () -> userServiceImpl.create(dto));

    then(userRepository).should(never()).save(any(User.class));
  }

  @Test
  void shouldReturnUserResponseDtoWhenGetUserById() {
    // Given
    var id = UUID.randomUUID();
    var response =
        new UserResponseDto(
            id,
            "john@doe.com",
            "John",
            "Doe",
            false,
            UserType.PERSONAL,
            UserStatus.ACTIVE,
            Instant.now());

    given(userRepository.findResponseDtoById(id)).willReturn(Optional.of(response));

    // When
    var result = userServiceImpl.getById(id);

    // Then
    assertEquals(id, result.id());
  }

  @Test
  void shouldThrowUserNotFoundExceptionWhenGetUserById() {
    // Given
    var id = UUID.randomUUID();

    given(userRepository.findResponseDtoById(id)).willThrow(new UserNotFoundException(id));

    // When + Then
    assertThrows(UserNotFoundException.class, () -> userServiceImpl.getById(id));
  }

  @Test
  void shouldSetStatusToDeleteWhenDeleteUserById() {
    // Given
    var id = UUID.randomUUID();
    var email = "john@doe.com";
    var providerUserId = "1234567890";
    var user =
        User.builder()
            .id(id)
            .providerUserId(providerUserId)
            .email(email)
            .userType(UserType.PERSONAL)
            .firstName("John")
            .lastName("Doe")
            .userStatus(UserStatus.ACTIVE)
            .roles(Set.of(UserRole.CANDIDATE))
            .emailVerified(false)
            .createdAt(Instant.now())
            .build();

    given(userRepository.findById(id)).willReturn(Optional.of(user));

    // When
    userServiceImpl.deleteById(id);

    // Then
    assertEquals(UserStatus.DELETED, user.getUserStatus());
    assertEquals("deleted_" + email, user.getEmail());
    assertEquals("deleted_" + providerUserId, user.getProviderUserId());

    then(userRepository).should().save(any(User.class));
  }

  @Test
  void shouldThrowUserNotFoundExceptionWhenDeleteUserById() {
    // Given
    var id = UUID.randomUUID();

    given(userRepository.findById(id)).willThrow(new UserNotFoundException(id));

    // When & Then
    assertThrows(UserNotFoundException.class, () -> userServiceImpl.deleteById(id));
    then(userRepository).should(never()).save(any(User.class));
  }

  @Test
  void shouldThrowUserAlreadyDeletedExceptionWhenDeleteUserById() {
    // Given
    var id = UUID.randomUUID();
    var deletedUser =
        User.builder()
            .id(id)
            .providerUserId("delete_1234567890")
            .email("delete_john@doe.com")
            .userType(UserType.PERSONAL)
            .firstName("John")
            .lastName("Doe")
            .userStatus(UserStatus.DELETED)
            .roles(Set.of(UserRole.CANDIDATE))
            .emailVerified(false)
            .createdAt(Instant.now())
            .build();

    given(userRepository.findById(id)).willReturn(Optional.of(deletedUser));

    // When & Then
    assertThrows(UserAlreadyDeletedException.class, () -> userServiceImpl.deleteById(id));
    then(userRepository).should(never()).save(any(User.class));
  }
}
