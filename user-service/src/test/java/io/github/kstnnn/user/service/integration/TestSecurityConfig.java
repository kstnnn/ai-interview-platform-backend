package io.github.kstnnn.user.service.integration;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.time.Instant;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@TestConfiguration
public class TestSecurityConfig {

  private static final String SECRET =
      "test-secret-key-for-jwt-signing-must-be-at-least-256-bits!!";

  private static final SecretKeySpec SIGNING_KEY =
      new SecretKeySpec(SECRET.getBytes(), "HmacSHA256");

  @Bean
  JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withSecretKey(SIGNING_KEY).macAlgorithm(MacAlgorithm.HS256).build();
  }

  @Bean
  NimbusJwtEncoder jwtEncoder() {
    return new NimbusJwtEncoder(new ImmutableSecret<>(SIGNING_KEY));
  }

  public static String generateToken(
      String subject, String issuer, Map<String, Object> extraClaims) {
    var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(SIGNING_KEY));
    var claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(subject)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .claims(c -> c.putAll(extraClaims))
            .build();
    var header = JwsHeader.with(MacAlgorithm.HS256).build();
    return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }
}
