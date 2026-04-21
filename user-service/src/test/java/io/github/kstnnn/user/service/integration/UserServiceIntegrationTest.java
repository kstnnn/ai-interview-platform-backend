package io.github.kstnnn.user.service.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.kstnnn.user.service.dto.UserCreateRequestDto;
import io.github.kstnnn.user.service.model.User;
import io.github.kstnnn.user.service.model.UserRole;
import io.github.kstnnn.user.service.model.UserStatus;
import io.github.kstnnn.user.service.model.UserType;
import io.github.kstnnn.user.service.exception.UserAlreadyDeletedException;
import io.github.kstnnn.user.service.exception.UserAlreadyExistsException;
import io.github.kstnnn.user.service.exception.UserNotFoundException;
import io.github.kstnnn.user.service.repository.UserRepository;
import io.github.kstnnn.user.service.service.UserService;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
@Transactional
@Rollback
public class UserServiceIntegrationTest {
  @Container @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

  @Autowired private UserRepository userRepository;
  @Autowired private UserService userService;

  @Test
  void shouldCreateUserAndReturnResponseDtoWhenUserDoesNotExist() {
    // Given
    var request =
        new UserCreateRequestDto(
            "1234567890",
            "john@doe.com",
            UserType.PERSONAL,
            "John",
            "Doe",
            Set.of(UserRole.CANDIDATE));

    // When
    var response = userService.create(request);

    // Then
    assertNotNull(response.id());
    assertEquals(request.email(), response.email());

    var saved = userRepository.findById(response.id()).orElseThrow();

    assertEquals(saved.getProviderUserId(), request.providerUserId());
  }

  @Test
  void shouldThrowUserAlreadyExistsWhenCreateUserWithDuplicateEmail() {
    // Given
    var email = "john@doe.com";
    var user =
        User.builder()
            .providerUserId("1234567890")
            .email(email)
            .userType(UserType.PERSONAL)
            .userStatus(UserStatus.ACTIVE)
            .firstName("John")
            .lastName("Doe")
            .roles(Set.of(UserRole.CANDIDATE))
            .build();
    userRepository.saveAndFlush(user);

    var request =
        new UserCreateRequestDto(
            "1234567890", email, UserType.PERSONAL, "John", "Doe", Set.of(UserRole.CANDIDATE));

    // When & Then
    assertThrows(UserAlreadyExistsException.class, () -> userService.create(request));
  }

  @Test
  void shouldThrowUserAlreadyExistsWhenCreateUserWithDuplicateProviderUserId() {
    // Given
    var providerUserId = "john@doe.com";
    var user =
        User.builder()
            .providerUserId(providerUserId)
            .email("john@doe.com")
            .userType(UserType.PERSONAL)
            .userStatus(UserStatus.ACTIVE)
            .firstName("John")
            .lastName("Doe")
            .roles(Set.of(UserRole.CANDIDATE))
            .build();
    userRepository.saveAndFlush(user);

    var request =
        new UserCreateRequestDto(
            providerUserId,
            "john@doe.com",
            UserType.PERSONAL,
            "John",
            "Doe",
            Set.of(UserRole.CANDIDATE));

    // When & Then
    assertThrows(UserAlreadyExistsException.class, () -> userService.create(request));
  }

  @Test
  void shouldSetStatusToDeletedAndScrubDataWhenDeleteUserById() {
    // Given
    var user =
        User.builder()
            .providerUserId("1234567890")
            .email("john@doe.com")
            .userType(UserType.PERSONAL)
            .userStatus(UserStatus.ACTIVE)
            .firstName("John")
            .lastName("Doe")
            .roles(Set.of(UserRole.CANDIDATE))
            .build();
    userRepository.saveAndFlush(user);

    // When
    userService.deleteById(user.getId());

    var deletedUser = userRepository.findById(user.getId()).orElseThrow();

    // Then
    assertEquals(UserStatus.DELETED, deletedUser.getUserStatus());
    assertEquals("deleted_john@doe.com", deletedUser.getEmail());
    assertEquals("deleted_1234567890", deletedUser.getProviderUserId());
  }

  @Test
  void shouldThrowUserNotFoundExceptionWhenDeleteUserById() {
    // Given
    var id = UUID.randomUUID();

    // When & Then
    assertThrows(UserNotFoundException.class, () -> userService.deleteById(id));
  }

  @Test
  void shouldThrowUserAlreadyDeletedExceptionWhenDeleteUserById() {
    // Given
    var user =
        User.builder()
            .providerUserId("deleted_1234567890")
            .email("deleted_john@doe.com")
            .userType(UserType.PERSONAL)
            .userStatus(UserStatus.DELETED)
            .firstName("John")
            .lastName("Doe")
            .roles(Set.of(UserRole.CANDIDATE))
            .build();
    userRepository.saveAndFlush(user);

    // When & Then
    assertThrows(UserAlreadyDeletedException.class, () -> userService.deleteById(user.getId()));
  }

  @Test
  void shouldReturnUserResponseDtoWhenGetUserById() {
    // Given
    var user =
        User.builder()
            .providerUserId("1234567890")
            .email("john@doe.com")
            .userType(UserType.PERSONAL)
            .userStatus(UserStatus.ACTIVE)
            .firstName("John")
            .lastName("Doe")
            .roles(Set.of(UserRole.CANDIDATE))
            .build();
    userRepository.saveAndFlush(user);

    // When
    var response = userService.getById(user.getId());

    // Then
    assertEquals(user.getEmail(), response.email());
    assertEquals(user.getId(), response.id());
  }

  @Test
  void shouldThrowUserNotFoundExceptionWhenGetUserById() {
    // Given
    var id = UUID.randomUUID();

    // When & Then
    assertThrows(UserNotFoundException.class, () -> userService.getById(id));
  }

  @Test
  void shouldThrowUserNotFoundExceptionWhenGetUserByIdAndUserIsDeleted() {
    // Given
    var user =
        User.builder()
            .providerUserId("1234567890")
            .email("john@doe.com")
            .userType(UserType.PERSONAL)
            .userStatus(UserStatus.DELETED)
            .firstName("John")
            .lastName("Doe")
            .roles(Set.of(UserRole.CANDIDATE))
            .build();
    userRepository.saveAndFlush(user);

    // When & Then
    assertThrows(UserNotFoundException.class, () -> userService.getById(user.getId()));
  }
}
