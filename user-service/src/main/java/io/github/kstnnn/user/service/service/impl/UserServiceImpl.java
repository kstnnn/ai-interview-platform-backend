package io.github.kstnnn.user.service.service.impl;

import io.github.kstnnn.user.service.entity.User;
import io.github.kstnnn.user.service.repository.UserRepository;
import io.github.kstnnn.user.service.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public void create(User newUser) {
    userRepository.save(newUser);
  }

  @Override
  public void deleteById(UUID id) {}

  @Override
  public User getById(UUID id) {
    return userRepository.getReferenceById(id);
  }
}
