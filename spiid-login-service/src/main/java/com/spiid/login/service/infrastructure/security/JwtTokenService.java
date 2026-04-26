package com.spiid.login.service.infrastructure.security;

import com.spiid.login.service.application.dto.JwtProperties;
import com.spiid.login.service.application.dto.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Emite y valida JWT para SPIID.
 * - Access token: para Authorization (corto)
 * - Refresh token: para renovar access (largo) y se persiste en DB como hash
 */
@Component
public class JwtTokenService {

  private final JwtProperties props;
  private final SecretKey key;

  public JwtTokenService(JwtProperties props) {
    this.props = props;
    // Nota: secret debe ser largo (>=32 bytes para HS256)
    this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
  }

  public String createAccessToken(User user) {
    if (user == null) {
      throw new IllegalArgumentException("User requerido");
    }

    if (user.id() == null) {
      throw new IllegalStateException("User.id es null (usuario no persistido)");
    }

    if (user.tenantId() == null) {
      throw new IllegalStateException("User.tenantId es null");
    }

    Instant now = Instant.now();
    Instant exp = now.plusSeconds(props.accessTtlSeconds());

    List<String> roleKeys = user.roles() == null
            ? List.of()
            : user.roles().stream()
            .map(r -> r.key())
            .filter(Objects::nonNull)
            .distinct()
            .toList();

    return Jwts.builder()
            .issuer(props.issuer())
            .subject(String.valueOf(user.id()))
            .claim("tenantId", String.valueOf(user.tenantId()))
            .claim("typ", "access")
            .claim("roles", roleKeys)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(key, Jwts.SIG.HS256)
            .compact();
  }

  public String createRefreshToken(User user) {
    if (user == null) {
      throw new IllegalArgumentException("User requerido");
    }

    if (user.id() == null) {
      throw new IllegalStateException("User.id es null (usuario no persistido)");
    }

    if (user.tenantId() == null) {
      throw new IllegalStateException("User.tenantId es null");
    }

    Instant now = Instant.now();
    Instant exp = now.plusSeconds(props.refreshTtlSeconds());

    return Jwts.builder()
            .issuer(props.issuer())
            .subject(String.valueOf(user.id()))
            .claim("tenantId", String.valueOf(user.tenantId()))
            .claim("typ", "refresh")
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(key, Jwts.SIG.HS256)
            .compact();
  }

  /**
   * Valida un access token y devuelve sus claims.
   * Lanza excepción si es inválido.
   */
  public Claims parseAccessToken(String token) {
    Claims c = parse(token);
    String typ = c.get("typ", String.class);
    if (!"access".equals(typ)) {
      throw new JwtException("Token no es access token");
    }
    return c;
  }

  /**
   * Validación base (firma, exp, etc.)
   */
  private Claims parse(String token) {
    JwtParser parser = Jwts.parser().verifyWith(key).build();
    Jws<Claims> jws = parser.parseSignedClaims(token);
    Claims c = jws.getPayload();
    if (props.issuer() != null && !props.issuer().isBlank()) {
      String iss = c.getIssuer();
      if (!props.issuer().equals(iss)) {
        throw new JwtException("Issuer inválido");
      }
    }
    return c;
  }

}
