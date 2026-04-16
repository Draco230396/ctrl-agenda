package com.spiid.login.service.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Filtro Bearer JWT:
 * - Si hay Authorization: Bearer <token>, valida y coloca Authentication en el SecurityContext.
 * - Si no hay token, deja pasar (y la regla de HttpSecurity decide si se permite o no).
 */
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtTokenService jwt;

  public JwtAuthFilter(JwtTokenService jwt) {
    this.jwt = jwt;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring("Bearer ".length()).trim();
      try {
        Claims claims = jwt.parseAccessToken(token);

        //Despues de leer claims
        String tenantId = claims.get("tenantId", String.class);
        TenantContext.set(UUID.fromString(tenantId));

        UUID userId = UUID.fromString(claims.getSubject());

        // roles en el token: lista de strings
        List<SimpleGrantedAuthority> auths = new ArrayList<>();
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?> list) {
          for (Object r : list) {
            if (r != null) auths.add(new SimpleGrantedAuthority("ROLE_" + r.toString()));
          }
        }

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userId, null, auths);

        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (JwtException | IllegalArgumentException e) {
        // Token inválido: limpiamos contexto y dejamos que la security chain responda (401 si aplica)
        SecurityContextHolder.clearContext();
      }
    }

    filterChain.doFilter(request, response);
  }
}
