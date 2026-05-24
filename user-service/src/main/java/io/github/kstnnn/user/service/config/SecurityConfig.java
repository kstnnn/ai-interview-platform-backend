package io.github.kstnnn.user.service.config;

import io.github.kstnnn.user.service.converter.JwtGrantedAuthoritiesConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

  @Value("${ZITADEL_ISSUER_URI}")
  private String issuerUri;

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(t -> t.disable())
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers("/api/v1/users")
                    .permitAll()
                    .requestMatchers("/api/v1/users/by-provider-id/**")
                    .permitAll()
                    .requestMatchers("/api/v1/users/auth/by-provider-id/**")
                    .permitAll()
                    .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/v1/users/**")
                    .hasAnyRole("CANDIDATE", "MANAGER", "ADMIN")
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
    return http.build();
  }

  // @Bean
  // JwtDecoder jwtDecoder() {
  //   NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
  //   var validator =
  //       new DelegatingOAuth2TokenValidator<>(
  //           new JwtTimestampValidator(), new JwtIssuerValidator(issuerUri));
  //   decoder.setJwtValidator(validator);
  //   return decoder;
  // }

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
    return converter;
  }
}
