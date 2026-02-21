package io.github.kstnnn.user.service.entity

import io.github.kstnnn.user.service.enums.UserStatus
import io.github.kstnnn.user.service.enums.UserType
import jakarta.persistence.Column
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

    @Column(nullable = false)
    val providerUserId: String,

    @Column(nullable = false)
    val email: String,

    @Column(nullable = false)
    val userType: UserType,

    @Column(nullable = false)
    val userStatus: UserStatus,

    @Column(nullable = false)
    val createdAt: Instant,

    @Column(nullable = false)
    var firstName: String,

    @Column(nullable = true)
    var lastName: String?,

    @Column(nullable = false)
    var emailVerified: Boolean = false
) {
}