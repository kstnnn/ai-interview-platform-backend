package io.github.kstnnn.user.service.repository;

import io.github.kstnnn.user.service.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {}
