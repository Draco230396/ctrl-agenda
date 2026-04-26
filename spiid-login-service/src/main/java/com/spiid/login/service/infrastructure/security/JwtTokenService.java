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
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(props.accessTtlSeconds());

    List<String> roleKeys = user.roles() == null ? List.of()
        : user.roles().stream().map(r -> r.key()).distinct().toList();

    return Jwts.builder()
        .issuer(props.issuer())
        .subject(user.id().toString())
        .claim("typ", "access")
        .claim("roles", roleKeys)
        .claim("tenantId", user.tenantId().toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  public String createRefreshToken(User user) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(props.refreshTtlSeconds());

    return Jwts.builder()
        .issuer(props.issuer())
        .subject(user.id().toString())
        .claim("typ", "refresh")
        .claim("tenantId", user.tenantId().toString())
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
