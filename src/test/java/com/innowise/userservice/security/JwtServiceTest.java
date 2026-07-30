package com.innowise.userservice.security;

import com.innowise.userservice.exception.TokenLifetimeValidationException;
import io.jsonwebtoken.JwtException;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

  @InjectMocks private JwtService jwtService;

  private static final String VALID_SECRET = "my_secret_key_is_so_safe_and_at_least_32_bytes_long";

  @BeforeEach
  void setUp() {
    jwtService = new JwtService(VALID_SECRET);
  }

  @Test
  void should_successInitConstructor() {
    assertNotNull(jwtService);
  }

  @Test
  void should_throwIllegalArgumentException_withNullSecret() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> new JwtService(null));
    assertEquals("JWT secret must not be empty!", exception.getMessage());
  }

  @Test
  void should_throwIllegalArgumentException_withBlankSecret() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> new JwtService(" "));
    assertEquals("JWT secret must not be empty!", exception.getMessage());
  }

  @Test
  void should_throwIllegalArgumentException_withShortSecret() {
    String shortSecretKey = "too_short_secret_key";
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> new JwtService(shortSecretKey));
    assertEquals("JWT secret must be at least 32 bytes long!", exception.getMessage());
  }

  @Test
  void should_returnClaims_withValidToken() {
    String expectedSubject = "Ron Nick";
    SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));
    String validToken =
        Jwts.builder()
            .subject(expectedSubject)
            .expiration(new Date(System.currentTimeMillis() + 60 * 1000))
            .signWith(key)
            .compact();

    Claims claims = jwtService.parse(validToken);

    assertNotNull(claims);
    assertEquals(expectedSubject, claims.getSubject());
  }

  @Test
  void should_throwTokenLifetimeValidationException_withExpiredToken() {
    String expectedSubject = "Ron Nick";
    SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));
    String expiredToken =
        Jwts.builder()
            .subject(expectedSubject)
            .expiration(new Date(System.currentTimeMillis() - 60 * 1000))
            .signWith(key)
            .compact();

    TokenLifetimeValidationException exception =
        assertThrows(TokenLifetimeValidationException.class, () -> jwtService.parse(expiredToken));

    assertTrue(exception.getMessage().contains("The token has expired!"));
    assertNotNull(exception.getCause());
    assertInstanceOf(ExpiredJwtException.class, exception.getCause());
  }

  @Test
  void should_throwJwtException_withInvalidSign() {
    String wrongSecret = "another_secret_key_which_is_valid_but_wrong_for_this_test";
    SecretKey wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8));
    String tokenWithWrongSign = Jwts.builder().subject("Ron Nick").signWith(wrongKey).compact();

    JwtException exception =
        assertThrows(JwtException.class, () -> jwtService.parse(tokenWithWrongSign));

    assertEquals("Invalid JWT token data!", exception.getMessage());
  }

  @Test
  void should_throwJwtException_withInvalidToken() {
    String badToken = "wrong_and_invalid_token";

    JwtException exception = assertThrows(JwtException.class, () -> jwtService.parse(badToken));

    assertEquals("Invalid JWT token data!", exception.getMessage());
  }
}
