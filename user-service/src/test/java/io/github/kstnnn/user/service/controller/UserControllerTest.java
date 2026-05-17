package io.github.kstnnn.user.service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kstnnn.user.service.dto.UserCreateRequestDto;
import io.github.kstnnn.user.service.dto.UserResponseDto;
import io.github.kstnnn.user.service.exception.UserAlreadyDeletedException;
import io.github.kstnnn.user.service.exception.UserAlreadyExistsException;
import io.github.kstnnn.user.service.exception.UserNotFoundException;
import io.github.kstnnn.user.service.model.UserRole;
import io.github.kstnnn.user.service.model.UserStatus;
import io.github.kstnnn.user.service.model.UserType;
import io.github.kstnnn.user.service.repository.UserRepository;
import io.github.kstnnn.user.service.service.UserService;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private UserService userService;

  @MockitoBean private UserRepository userRepository;

  @Test
  void shouldReturnUserResponseDto() throws Exception {
    // Arrange
    var id = UUID.randomUUID();
    var email = "john@doe.com";
    var response =
        new UserResponseDto(
            id, email, "John", "Doe", false, UserType.PERSONAL, UserStatus.ACTIVE, Instant.now());

    when(userService.getById(id)).thenReturn(response);

    // Act & Assert
    mockMvc
        .perform(get("/api/v1/users/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.email").value(email));
    verify(userService).getById(id);
  }

  @Test
  void shouldCreateUser() throws Exception {
    // Arrange
    var userId = UUID.randomUUID();
    var request =
        new UserCreateRequestDto(
            "1234567890",
            "john@doe.com",
            UserType.PERSONAL,
            "John",
            "Doe",
            Set.of(UserRole.MANAGER));
    var response =
        new UserResponseDto(
            userId,
            "john@doe.com",
            "John",
            "Doe",
            false,
            UserType.PERSONAL,
            UserStatus.ACTIVE,
            Instant.now());

    when(userService.create(any(UserCreateRequestDto.class))).thenReturn(response);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.email").value("john@doe.com"));
    verify(userService).create(any(UserCreateRequestDto.class));
  }

  @Test
  void shouldDeleteUser() throws Exception {
    // Arrange
    var id = UUID.randomUUID();
    doNothing().when(userService).deleteById(id);

    // Act & Assert
    mockMvc.perform(delete("/api/v1/users/{id}", id)).andExpect(status().isNoContent());
    verify(userService).deleteById(id);
  }

  @Test
  void shouldReturn404WhenUserNotFound() throws Exception {
    // Arrange
    var id = UUID.randomUUID();
    when(userService.getById(id)).thenThrow(new UserNotFoundException(id));

    // Act & Assert
    mockMvc
        .perform(get("/api/v1/users/{id}", id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value(String.format("User not found with id %s", id)));
  }

  @Test
  void shouldReturn400WhenUserAlreadyExists() throws Exception {
    // Arrange
    var request =
        new UserCreateRequestDto(
            "1234567890",
            "john@doe.com",
            UserType.PERSONAL,
            "John",
            "Doe",
            Set.of(UserRole.CANDIDATE));

    when(userService.create(any(UserCreateRequestDto.class)))
        .thenThrow(new UserAlreadyExistsException("email", "john@doe.com"));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("User with email '******oe.com' already exists"));
  }

  @Test
  void shouldReturn400WhenUserAlreadyDeleted() throws Exception {
    // Arrange
    var id = UUID.randomUUID();
    doThrow(new UserAlreadyDeletedException(id)).when(userService).deleteById(id);

    // Act & Assert
    mockMvc
        .perform(delete("/api/v1/users/{id}", id))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error").value(String.format("User with id %s is already deleted.", id)));
  }

  @Test
  void shouldReturn400WhenValidationFails() throws Exception {
    // Arrange
    var request =
        """
        {
          "providerUserId": "",
          "email": "invalid",
          "firstName": "",
          "userType": null,
          "roles": []
        }
        """;

    // Act & Assert
    mockMvc
        .perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Validation failed"))
        .andExpect(jsonPath("$.details.providerUserId").value("Provider id is required."))
        .andExpect(jsonPath("$.details.email").value("Email must be valid."))
        .andExpect(jsonPath("$.details.firstName").value("First name is required."))
        .andExpect(jsonPath("$.details.roles").value("At least one role is required."));
  }
}
