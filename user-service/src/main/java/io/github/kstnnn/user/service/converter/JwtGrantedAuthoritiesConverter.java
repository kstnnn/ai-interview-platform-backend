package io.github.kstnnn.user.service.converter;

import io.github.kstnnn.user.service.model.UserStatus;
import io.github.kstnnn.user.service.repository.UserRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtGrantedAuthoritiesConverter
    implements Converter<Jwt, Collection<GrantedAuthority>> {

  private final UserRepository userRepository;

  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    var providerUserId = jwt.getSubject();

    var userOpt = userRepository.findUserByProviderUserId(providerUserId);
    if (userOpt.isEmpty()) {
      return Collections.emptySet();
    }

    var user = userOpt.get();
    if (user.getUserStatus() != UserStatus.ACTIVE || user.getRoles() == null) {
      return Collections.emptySet();
    }

    return user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .collect(Collectors.toSet());
  }
}
