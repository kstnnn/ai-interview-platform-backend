package io.github.kstnnn.user.service.repository;

import io.github.kstnnn.user.service.dto.UserResponseDto;
import io.github.kstnnn.user.service.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<UserResponseDto> findResponseDtoById(UUID id);

  Optional<User> findUserByProviderUserId(String id);

  boolean existsById(UUID id);
}
