package com.innowise.userservice.security;

import com.innowise.userservice.exception.TokenLifetimeValidationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey secretKey;

  public JwtService(@Value("${jwt.secret-key}") String secret) {
    if (secret == null || secret.isBlank()) {
      throw new IllegalArgumentException("JWT secret must not be empty!");
    }
    byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

    if (secretBytes.length < 32) {
      throw new IllegalArgumentException("JWT secret must be at least 32 bytes long!");
    }
    this.secretKey = Keys.hmacShaKeyFor(secretBytes);
  }

  public Claims parse(String token) {
    try {
      return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException e) {
      throw new TokenLifetimeValidationException("The token has expired! ", e);
    } catch (JwtException | IllegalArgumentException e) {
      throw new JwtException("Invalid JWT token data!", e);
    }
  }
}
