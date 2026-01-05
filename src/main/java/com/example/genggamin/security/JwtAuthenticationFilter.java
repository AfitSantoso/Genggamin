package com.example.genggamin.security;

import com.example.genggamin.service.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final TokenBlacklistService tokenBlacklistService;

  public JwtAuthenticationFilter(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
    this.jwtUtil = jwtUtil;
    this.tokenBlacklistService = tokenBlacklistService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null) {
      if (!header.startsWith("Bearer ")) {
        // header present but not bearer -> respond missing/bearer required
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        String body =
            "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authorization header is missing or bearer token is required\"}";
        response.getWriter().write(body);
        return;
      }

      String token = header.substring(7);
      try {
        // Check if token is blacklisted
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          response.setContentType("application/json");
          String body =
              "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Token has been revoked (logged out)\"}";
          response.getWriter().write(body);
          return;
        }

        String username = jwtUtil.getUsernameFromToken(token);
        java.util.Set<String> roles = jwtUtil.getRolesFromToken(token);

        // Convert roles to authorities with ROLE_ prefix
        List<SimpleGrantedAuthority> authorities =
            roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(java.util.stream.Collectors.toList());

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
      } catch (ExpiredJwtException eje) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        String body =
            "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid or expired bearer token\"}";
        response.getWriter().write(body);
        return;
      } catch (JwtException | IllegalArgumentException e) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        String body =
            "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid or expired bearer token\"}";
        response.getWriter().write(body);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}
