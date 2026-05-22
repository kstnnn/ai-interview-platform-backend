package io.github.kstnnn.ai.interview.service.config;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@RequiredArgsConstructor
@Slf4j
public class WebSocketJwtInterceptor implements HandshakeInterceptor {

  private final JwtDecoder jwtDecoder;

  @Override
  public boolean beforeHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes) {
    String query = request.getURI().getQuery();
    if (query == null || !query.contains("token=")) {
      log.warn("WebSocket handshake rejected: missing token");
      return false;
    }

    String token = query.substring(query.indexOf("token=") + 6);
    if (token.contains("&")) {
      token = token.substring(0, token.indexOf("&"));
    }

    try {
      var jwt = jwtDecoder.decode(token);
      attributes.put("principal", jwt.getSubject());
      attributes.put("jwt", jwt);
      log.info("WebSocket handshake accepted for subject={}", jwt.getSubject());
      return true;
    } catch (JwtException e) {
      log.warn("WebSocket handshake rejected: invalid token", e);
      return false;
    }
  }

  @Override
  public void afterHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Exception exception) {}
}
