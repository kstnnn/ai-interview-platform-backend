package io.github.kstnnn.user.service.service;

import io.github.kstnnn.user.service.entity.User;
import java.util.UUID;

public interface UserService {
  User getById(UUID id);

  void create(User newUser);

  void deleteById(UUID id);
}
