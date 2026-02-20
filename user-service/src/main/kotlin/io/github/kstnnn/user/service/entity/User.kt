package io.github.kstnnn.user.service.entity

import io.github.kstnnn.user.service.enums.UserStatus
import io.github.kstnnn.user.service.enums.UserType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    val id: UUID,
    val providerUserId: String,
    val email: String,
    val userType: UserType,
    val userStatus: UserStatus,
    val createdAt: Instant,
    var firstName: String,
    var lastName: String?,
    var emailVerified: Boolean = false
) {
}