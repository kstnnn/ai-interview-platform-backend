package io.github.kstnnn.user.service.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kstnnn.user.service.model.User;
import io.github.kstnnn.user.service.model.UserRole;
import io.github.kstnnn.user.service.model.UserStatus;
import io.github.kstnnn.user.service.model.UserType;
import io.github.kstnnn.user.service.repository.UserRepository;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest
@Testcontainers
public class UserRepositoryIntegrationTest {

  @Container @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

  @Autowired private UserRepository userRepository;

  @Test
  void shouldSaveUserAndGenerateIdWhenValidDataProvided() {
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

    // When
    var saved = userRepository.save(user);

    // Then
    assertNotNull(saved.getId());
    assertNotNull(saved.getCreatedAt());

    assertEquals(1, saved.getRoles().size());
    assertTrue(saved.getRoles().contains(UserRole.CANDIDATE));

    assertFalse(saved.isEmailVerified());
    assertEquals(UserStatus.ACTIVE, saved.getUserStatus());

    assertEquals("john@doe.com", saved.getEmail());

    var found = userRepository.findById(saved.getId()).orElseThrow();
    assertEquals("John", found.getFirstName());
    assertEquals(UserType.PERSONAL, found.getUserType());
  }

  @Test
  void shouldThrowDataIntegrityViolationExceptionWhenEmailIsDuplicate() {
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

    var duplicate =
        User.builder()
            .providerUserId("34768347")
            .email(email)
            .userType(UserType.BUSINESS)
            .userStatus(UserStatus.ACTIVE)
            .firstName("Jane")
            .lastName("Doe")
            .roles(Set.of(UserRole.MANAGER))
            .build();

    // When & Then
    assertThrows(
        DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(duplicate));
  }

  @Test
  void shouldReturnResponseDtoWhenUserExists() {
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
    var dto = userRepository.findResponseDtoById(user.getId()).orElseThrow();

    // Then
    assertEquals(user.getId(), dto.id());
    assertEquals(user.getEmail(), dto.email());
    assertEquals(user.getUserType(), dto.userType());
    assertEquals(user.getUserStatus(), dto.userStatus());
    assertEquals(user.getFirstName(), dto.firstName());
    assertEquals(user.getLastName(), dto.lastName());
    assertFalse(dto.emailVerified());
  }

  @Test
  void shouldReturnEmptyWhenUserIsDeleted() {
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

    // When
    var response = userRepository.findResponseDtoById(user.getId());

    // Then
    assertTrue(response.isEmpty());
  }

  @Test
  void shouldFindUserByProviderUserIdWhenUserExists() {
    // Given
    var providerUserId = "1234567890";
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

    // When
    var found = userRepository.findUserByProviderUserId(providerUserId).orElseThrow();

    // Then
    assertEquals(providerUserId, found.getProviderUserId());
    assertEquals(user.getEmail(), found.getEmail());
    assertEquals(user.getId(), found.getId());
  }

  @Test
  void shouldReturnTrueWhenUserExistsByEmail() {
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

    // When & Then
    assertTrue(userRepository.existsByEmail(email));
  }

  @Test
  void shouldReturnFalseWhenUserNotExistsByEmail() {
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

    // When & Then
    assertFalse(userRepository.existsByEmail("jane@doe.com"));
  }

  @Test
  void shouldReturnTrueWhenUserExistsByProviderUserId() {
    // Given
    var providerUserId = "1234567890";
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

    // When & Then
    assertTrue(userRepository.existsByProviderUserId(providerUserId));
  }

  @Test
  void shouldReturnFalseWhenUserNotExistsByProviderUserId() {
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

    // When & Then
    assertFalse(userRepository.existsByProviderUserId("0987654321"));
  }
}
