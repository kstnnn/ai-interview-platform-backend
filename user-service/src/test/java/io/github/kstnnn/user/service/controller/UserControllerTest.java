package io.github.kstnnn.user.service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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
import io.github.kstnnn.user.service.enums.UserRole;
import io.github.kstnnn.user.service.enums.UserStatus;
import io.github.kstnnn.user.service.enums.UserType;
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
}
