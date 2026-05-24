package io.github.kstnnn.user.service.config;

import io.github.kstnnn.user.service.model.UserRole;
import io.github.kstnnn.user.service.model.UserStatus;
import io.github.kstnnn.user.service.repository.UserRepository;
import java.util.HashSet;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

  private final UserRepository userRepository;

  @Value("${app.admin.provider-user-ids:${APP_ADMIN_PROVIDER_USER_IDS:}}")
  private String adminProviderUserIds;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    var providerUserIds =
        Arrays.stream(adminProviderUserIds.split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .distinct()
            .toList();

    if (providerUserIds.isEmpty()) {
      return;
    }

    for (var providerUserId : providerUserIds) {
      userRepository
          .findUserByProviderUserId(providerUserId)
          .ifPresentOrElse(
              user -> {
                if (user.getUserStatus() == UserStatus.DELETED) {
                  log.warn("Skipping deleted bootstrap admin user providerUserId={}", providerUserId);
                  return;
                }
                var roles = user.getRoles() == null ? new HashSet<UserRole>() : new HashSet<>(user.getRoles());
                if (roles.add(UserRole.ADMIN)) {
                  user.setRoles(roles);
                  userRepository.save(user);
                  log.info("Granted ADMIN role to providerUserId={}", providerUserId);
                }
              },
              () -> log.warn("Bootstrap admin providerUserId={} not found", providerUserId));
    }
  }
}
