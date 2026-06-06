package com.innowise.userservice.exception;

import io.jsonwebtoken.ExpiredJwtException;

public class TokenLifetimeValidationException extends RuntimeException {
  public TokenLifetimeValidationException(String message, ExpiredJwtException e) {
    super(message, e);
  }

  public TokenLifetimeValidationException(String message, Throwable t) {
    super(message, t);
  }
}
