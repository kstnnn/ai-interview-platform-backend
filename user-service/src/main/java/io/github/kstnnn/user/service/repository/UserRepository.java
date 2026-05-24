package io.github.kstnnn.user.service.repository;

import io.github.kstnnn.user.service.dto.UserResponseDto;
import io.github.kstnnn.user.service.model.UserRole;
import io.github.kstnnn.user.service.model.UserStatus;
import io.github.kstnnn.user.service.model.UserType;
import io.github.kstnnn.user.service.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  @Query(
      "SELECT new io.github.kstnnn.user.service.dto.UserResponseDto("
          + "u.id, u.email, u.firstName, u.lastName, u.emailVerified, "
          + "u.userType, u.userStatus, u.createdAt) "
          + "FROM User u "
          + "WHERE u.providerUserId = :providerUserId AND u.userStatus <> 'DELETED'")
  Optional<UserResponseDto> findResponseDtoByProviderUserId(String providerUserId);

  boolean existsByProviderUserId(String providerUserId);

  @Query(
      "SELECT DISTINCT u FROM User u LEFT JOIN u.roles r "
      + "WHERE u.userStatus <> 'DELETED' "
          + "AND (:userType IS NULL OR u.userType = :userType) "
          + "AND (:userStatus IS NULL OR u.userStatus = :userStatus) "
          + "AND (:role IS NULL OR r = :role)")
  Page<User> findAdminUsers(
      @Param("userType") UserType userType,
      @Param("userStatus") UserStatus userStatus,
      @Param("role") UserRole role,
      Pageable pageable);

  @Query(
      "SELECT DISTINCT u FROM User u LEFT JOIN u.roles r "
          + "WHERE u.userStatus <> 'DELETED' "
          + "AND (LOWER(u.email) LIKE CONCAT('%', :search, '%') "
          + "OR LOWER(u.firstName) LIKE CONCAT('%', :search, '%') "
          + "OR LOWER(COALESCE(u.lastName, '')) LIKE CONCAT('%', :search, '%')) "
          + "AND (:userType IS NULL OR u.userType = :userType) "
          + "AND (:userStatus IS NULL OR u.userStatus = :userStatus) "
          + "AND (:role IS NULL OR r = :role)")
  Page<User> findAdminUsersBySearch(
      @Param("search") String search,
      @Param("userType") UserType userType,
      @Param("userStatus") UserStatus userStatus,
      @Param("role") UserRole role,
      Pageable pageable);
}
