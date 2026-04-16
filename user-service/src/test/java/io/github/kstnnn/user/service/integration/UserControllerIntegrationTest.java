package io.github.kstnnn.user.service.integration;

import io.github.kstnnn.user.service.dto.UserCreateRequestDto;
import io.github.kstnnn.user.service.entity.User;
import io.github.kstnnn.user.service.enums.UserRole;
import io.github.kstnnn.user.service.enums.UserStatus;
import io.github.kstnnn.user.service.enums.UserType;
import io.github.kstnnn.user.service.repository.UserRepository;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureRestTestClient
@Import(TestSecurityConfig.class)
public class UserControllerIntegrationTest {

  @Container @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

  @Autowired private RestTestClient restTestClient;

  @Autowired private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Test
  void shouldReturn200WhenGetUser() {
    // Given
    var saved =
        User.builder()
            .providerUserId("1234567890")
            .email("johndoe@example.com")
            .userType(UserType.PERSONAL)
            .userStatus(UserStatus.ACTIVE)
            .firstName("John")
            .lastName("Doe")
            .roles(Set.of(UserRole.CANDIDATE))
            .build();
    userRepository.saveAndFlush(saved);

    var token =
        TestSecurityConfig.generateToken(saved.getProviderUserId(), "test-issuer", Map.of());

    // When & Then
    restTestClient
        .get()
        .uri("/api/v1/users/" + saved.getId())
        .header("Authorization", "Bearer " + token)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.email")
        .isEqualTo(saved.getEmail());
  }

  @Test
  void shouldReturn401WhenGetUserNoToken() {
    // Given
    var id = UUID.randomUUID();

    // When & Then
    restTestClient.get().uri("/api/v1/users/" + id).exchange().expectStatus().isUnauthorized();
  }

  @Test
  void shouldReturn404WhenUserNotFound() {
    // Given
    var saved =
        User.builder()
            .providerUserId("1234567890")
            .email("johndoe@example.com")
            .userType(UserType.PERSONAL)
            .userStatus(UserStatus.ACTIVE)
            .firstName("John")
            .lastName("Doe")
            .roles(Set.of(UserRole.CANDIDATE))
            .build();
    userRepository.saveAndFlush(saved);

    var token =
        TestSecurityConfig.generateToken(saved.getProviderUserId(), "test-issuer", Map.of());

    // When & Then
    restTestClient
        .get()
        .uri("/api/v1/users/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + token)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void shouldReturn201WhenCreateUser() {
    // Given
    var request =
        new UserCreateRequestDto(
            "1234567890",
            "john@doe.com",
            UserType.PERSONAL,
            "John",
            "Doe",
            Set.of(UserRole.MANAGER));

    // When & Then
    restTestClient
        .post()
        .uri("/api/v1/users")
        .body(request)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.email")
        .isEqualTo(request.email());
  }

  @Test
  void shouldReturn400WhenUserAlreadyExistsWithDuplicateEmail() {
    // Given
    var email = "john@doe.com";
    var saved =
        User.builder()
            .providerUserId("1234567890")
            .email(email)
            .userType(UserType.PERSONAL)
            .userStatus(UserStatus.ACTIVE)
            .firstName("John")
            .lastName("Doe")
            .roles(Set.of(UserRole.CANDIDATE))
            .build();
    userRepository.saveAndFlush(saved);

    var request =
        new UserCreateRequestDto(
            "1234567890", email, UserType.PERSONAL, "John", "Doe", Set.of(UserRole.MANAGER));

    // When & Then
    restTestClient
        .post()
        .uri("/api/v1/users")
        .body(request)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void shouldReturn400WhenUserAlreadyExistsWithDuplicateProviderUserId() {
    // Given
    var providerUserId = "1234567890";
    var saved =
        User.builder()
            .providerUserId(providerUserId)
            .email("john@doe.com")
            .userType(UserType.PERSONAL)
            .userStatus(UserStatus.ACTIVE)
            .firstName("John")
            .lastName("Doe")
            .roles(Set.of(UserRole.CANDIDATE))
            .build();
    userRepository.saveAndFlush(saved);

    var request =
        new UserCreateRequestDto(
            providerUserId,
            "john@doe.example",
            UserType.PERSONAL,
            "John",
            "Doe",
            Set.of(UserRole.MANAGER));

    // When & Then
    restTestClient
        .post()
        .uri("/api/v1/users")
        .body(request)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void shouldReturn204WhenDeleteUser() {
    // Given
    var saved =
        User.builder()
            .providerUserId("1234567890")
            .email("john@doe.com")
            .userType(UserType.PERSONAL)
            .userStatus(UserStatus.ACTIVE)
            .firstName("John")
            .lastName("Doe")
            .roles(Set.of(UserRole.CANDIDATE))
            .build();
    userRepository.saveAndFlush(saved);

    var token =
        TestSecurityConfig.generateToken(saved.getProviderUserId(), "test-issuer", Map.of());

    // When & Then
    restTestClient
        .delete()
        .uri("/api/v1/users/" + saved.getId())
        .header("Authorization", "Bearer " + token)
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  void shouldReturn400WhenUserAlreadyDeleted() {
    // Given
    var deleted =
        User.builder()
            .providerUserId("deleted_1234567890")
            .email("deleted_john@doe.com")
            .userType(UserType.PERSONAL)
            .userStatus(UserStatus.DELETED)
            .firstName("John")
            .lastName("Doe")
            .roles(Set.of(UserRole.CANDIDATE))
            .build();
    userRepository.saveAndFlush(deleted);

    var token =
        TestSecurityConfig.generateToken(deleted.getProviderUserId(), "test-issuer", Map.of());

    // When & Then
    restTestClient
        .delete()
        .uri("/api/v1/users/" + deleted.getId())
        .header("Authorization", "Bearer " + token)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void shouldReturn404WhenDeleteUserNotFound() {
    // Given
    var saved =
        User.builder()
            .providerUserId("1234567890")
            .email("johndoe@example.com")
            .userType(UserType.PERSONAL)
            .userStatus(UserStatus.ACTIVE)
            .firstName("John")
            .lastName("Doe")
            .roles(Set.of(UserRole.CANDIDATE))
            .build();
    userRepository.saveAndFlush(saved);

    var token =
        TestSecurityConfig.generateToken(saved.getProviderUserId(), "test-issuer", Map.of());

    // When & Then
    restTestClient
        .delete()
        .uri("/api/v1/users/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + token)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void shouldReturn401WhenDeleteUserNoToken() {
    // Given
    var id = UUID.randomUUID();

    // When & Then
    restTestClient.delete().uri("/api/v1/users/" + id).exchange().expectStatus().isUnauthorized();
  }
}
