package com.innowise.userservice.exception;

public class CardLimitException extends RuntimeException {
  public CardLimitException() {
    super("User may have only 5 or less active cards!");
  }
}
