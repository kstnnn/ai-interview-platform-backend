package io.github.kstnnn.user.service.repository;

import io.github.kstnnn.user.service.dto.UserResponseDto;
import io.github.kstnnn.user.service.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {
  @Query(
      "SELECT new io.github.kstnnn.user.service.dto.UserResponseDto("
          + "u.id, u.email, u.firstName, u.lastName, u.emailVerified, "
          + "u.userType, u.userStatus, u.createdAt) "
          + "FROM User u "
          + "WHERE u.id = :id AND u.userStatus <> 'DELETED'")
  Optional<UserResponseDto> findResponseDtoById(@Param("id") UUID id);

  Optional<User> findUserByProviderUserId(String id);

  boolean existsByEmail(String email);
}
