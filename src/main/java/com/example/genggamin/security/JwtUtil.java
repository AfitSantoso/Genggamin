package com.example.genggamin.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${app.jwt.secret:secret-key-please-change-this}") String secret,
                   @Value("${app.jwt.expiration:86400000}") long expirationMs) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            try {
                java.security.MessageDigest sha = java.security.MessageDigest.getInstance("SHA-256");
                keyBytes = sha.digest(keyBytes);
            } catch (java.security.NoSuchAlgorithmException e) {
                throw new IllegalStateException("Failed to create JWT key", e);
            }
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, Set<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        JwtBuilder b = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("roles", roles.stream().collect(Collectors.toList()))
                .signWith(key, SignatureAlgorithm.HS256);

        return b.compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    @SuppressWarnings("unchecked")
    public Set<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        java.util.List<String> rolesList = claims.get("roles", java.util.List.class);
        if (rolesList != null) {
            return rolesList.stream().collect(Collectors.toSet());
        }
        return java.util.Collections.emptySet();
    }
}
