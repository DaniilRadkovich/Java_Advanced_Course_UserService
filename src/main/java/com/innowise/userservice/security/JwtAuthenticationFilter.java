package com.innowise.userservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.exception.TokenLifetimeValidationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String jwt = authHeader.substring(7);
      Claims claims = jwtService.parse(jwt);
      String username = claims.getSubject();

      if (username == null || username.isBlank()) {
        throw new JwtException("Username must not be empty!");
      }

      String tokenType = claims.get("type", String.class);

      if (tokenType == null || !tokenType.equals("access")) {
        throw new JwtException("Wrong token type used for authentication!");
      }

      if (SecurityContextHolder.getContext().getAuthentication() == null) {

        String role = claims.get("role", String.class);
        UUID userId = UUID.fromString(claims.get("id", String.class));

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(userId, null, authorities);

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      }
      filterChain.doFilter(request, response);
    } catch (TokenLifetimeValidationException e) {
      sendErrorResponse(
          response,
          request.getRequestURI(),
          HttpServletResponse.SC_UNAUTHORIZED,
          "Unauthorized!",
          "The token has expired! Required token refresh!");
    } catch (JwtException e) {
      sendErrorResponse(
          response,
          request.getRequestURI(),
          HttpServletResponse.SC_UNAUTHORIZED,
          "Forbidden!",
          "Invalid token data!");
    } catch (Exception e) {
      SecurityContextHolder.clearContext();
      sendErrorResponse(
          response,
          request.getRequestURI(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Server Error!",
          "Something went wrong!");
    }
  }

  private void sendErrorResponse(
      HttpServletResponse response, String path, int status, String error, String message)
      throws IOException {
    SecurityContextHolder.clearContext();
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now().toString());
    body.put("path", path);
    body.put("status", status);
    body.put("error", error);
    body.put("message", message);

    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
