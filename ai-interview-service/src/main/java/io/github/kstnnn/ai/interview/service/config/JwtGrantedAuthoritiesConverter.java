package io.github.kstnnn.ai.interview.service.config;

import io.github.kstnnn.ai.interview.service.dto.UserAuthLookupDto;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class JwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  private final RestClient restClient;

  public JwtGrantedAuthoritiesConverter(
      RestClient.Builder restClientBuilder,
      @Value("${app.user-service.base-url:http://localhost:8080}") String userServiceBaseUrl) {
    this.restClient = restClientBuilder.baseUrl(userServiceBaseUrl).build();
  }

  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    try {
      var user =
          restClient
              .get()
              .uri("/api/v1/users/auth/by-provider-id/{providerUserId}", jwt.getSubject())
              .retrieve()
              .body(UserAuthLookupDto.class);
      if (user == null || user.roles() == null || !"ACTIVE".equals(user.userStatus())) {
        return Collections.emptySet();
      }
      return user.roles().stream()
          .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
          .collect(Collectors.toSet());
    } catch (RestClientException ex) {
      return Collections.emptySet();
    }
  }
}
