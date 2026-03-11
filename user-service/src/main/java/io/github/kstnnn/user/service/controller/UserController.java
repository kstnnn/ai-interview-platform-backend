package io.github.kstnnn.user.service.controller;

import io.github.kstnnn.user.service.dto.UserCreateRequestDto;
import io.github.kstnnn.user.service.dto.UserResponseDto;
import io.github.kstnnn.user.service.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  @GetMapping("/{id}")
  ResponseEntity<UserResponseDto> getUser(@PathVariable UUID id) {
    return ResponseEntity.ok(userService.getById(id));
  }

  @PostMapping
  ResponseEntity<UserResponseDto> createUser(
      @AuthenticationPrincipal Jwt jwt, @RequestBody UserCreateRequestDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(jwt, dto));
  }

  @DeleteMapping("/{id}")
  ResponseEntity<Void> deleteById(@PathVariable UUID id) {
    userService.deleteById(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
