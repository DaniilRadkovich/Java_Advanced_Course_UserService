package com.innowise.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class InternalKeyFilter extends OncePerRequestFilter {

  @Value("${INTERNAL_KEY}")
  private String internalKey;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String uri = request.getRequestURI();

    if (!uri.startsWith("/api/v1/internal")) {
      filterChain.doFilter(request, response);
      return;
    }

    String header = request.getHeader("X-Internal-Key");
    if (!internalKey.equals(header)) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid internal key");
      return;
    }
    filterChain.doFilter(request, response);
  }
}
