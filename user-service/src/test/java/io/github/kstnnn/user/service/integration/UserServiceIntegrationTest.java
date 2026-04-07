package io.github.kstnnn.user.service.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.kstnnn.user.service.dto.UserCreateRequestDto;
import io.github.kstnnn.user.service.entity.User;
import io.github.kstnnn.user.service.enums.UserRole;
import io.github.kstnnn.user.service.enums.UserStatus;
import io.github.kstnnn.user.service.enums.UserType;
import io.github.kstnnn.user.service.exception.UserAlreadyExistsException;
import io.github.kstnnn.user.service.repository.UserRepository;
import io.github.kstnnn.user.service.service.UserService;
import java.util.Set;
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
  void shouldThrowWhenUserAlreadyExists() {
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

    var request =
        new UserCreateRequestDto(
            "1234567890",
            "john@doe.com",
            UserType.PERSONAL,
            "John",
            "Doe",
            Set.of(UserRole.CANDIDATE));

    // When & Then
    assertThrows(UserAlreadyExistsException.class, () -> userService.create(request));
  }
}
