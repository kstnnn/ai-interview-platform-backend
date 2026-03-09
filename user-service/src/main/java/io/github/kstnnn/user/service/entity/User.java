package io.github.kstnnn.user.service.entity;

import io.github.kstnnn.user.service.enums.UserStatus;
import io.github.kstnnn.user.service.enums.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String providerUserId;

  @Version
  @Column(nullable = false)
  private Long version;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private UserType userType;

  @Column(nullable = false)
  private UserStatus userStatus;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = true)
  private String lastName;

  @Column(nullable = false)
  @Builder.Default
  private boolean emailVerified = false;
}
