package io.github.kstnnn.ai.interview.service.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

  public SecurityConfig(JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter) {
    this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
  }

  @Value("${ZITADEL_ISSUER_URI}")
  private String issuerUri;

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(t -> t.disable())
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/ws/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/interviews")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/technologies", "/api/v1/technologies/grouped")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/internal/interviews/*/report")
                    .permitAll()
                    .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .hasAnyRole("CANDIDATE", "MANAGER", "ADMIN"))
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
    return http.build();
  }

  @Bean
  org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
      jwtAuthenticationConverter() {
    var converter =
        new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
    return converter;
  }

  @Bean
  JwtDecoder jwtDecoder() {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    var validator =
        new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(), new JwtIssuerValidator(issuerUri));
    decoder.setJwtValidator(validator);
    return decoder;
  }
}
