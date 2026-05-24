package io.github.kstnnn.organization.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

  public SecurityConfig(JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter) {
    this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(t -> t.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/vacancies/public")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/vacancies/public/*")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/vacancies/*")
                    .permitAll()
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
}
