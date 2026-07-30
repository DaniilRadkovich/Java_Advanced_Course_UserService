package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.PaymentCardCreateRequest;
import com.innowise.userservice.model.dto.PaymentCardDto;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

/**
 * Payment card controller interface for managing payment cards in the application. Provides
 * endpoints for creating, finding, updating, activating/deactivating the status of payment card and
 * deleting. URL prefix: /api/v1/cards
 */
public interface PaymentCardController {

  /**
   * Creates a new payment card linked to a specific user. URL: /api/v1/cards/{userId} with POST
   * method.
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role. Input data is validated
   * automatically.
   *
   * @param userId the unique identifier (UUID) of the user who will own the card.
   * @param request the PaymentCardCreateRequest containing necessary card details (id, number,
   *     holder, expiration date).
   * @return ResponseEntity containing the created PaymentCardDto (userId, number, holder,
   *     expiration date, active) with HTTP status 201 (Created).
   */
  ResponseEntity<PaymentCardDto> createPaymentCard(UUID userId, PaymentCardCreateRequest request);

  /**
   * Finds a payment card by its cardId. URL: /api/v1/cards/{cardId} with GET method.
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param cardId the unique identifier (UUID) of the payment card.
   * @return ResponseEntity containing the found PaymentCardDto (userId, number, holder, expiration
   *     date, active).
   */
  ResponseEntity<PaymentCardDto> getCardById(UUID cardId);

  /**
   * Finds a paginated list of all payment cards with optional filtering by holder name, owner name,
   * or owner surname. URL: /api/v1/cards with GET method.
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param holder optional filter matching the name printed on the card.
   * @param name optional filter matching the card owner's first name.
   * @param surname optional filter matching the card owner's surname.
   * @param pageable pagination and sorting parameters.
   * @return ResponseEntity containing a Page of PaymentCardDto (userId, number, holder, expiration
   *     date, active).
   */
  ResponseEntity<Page<PaymentCardDto>> getAllCards(
      String holder, String name, String surname, Pageable pageable);

  /**
   * Finds all payment cards associated with a specific user ID. URL: /api/v1/cards/user/{userId}
   * with GET method.
   *
   * <p>Access is allowed for users with the 'ADMIN' role or for the owners of the cards matching
   * the requested user ID.
   *
   * @param userId the unique identifier (UUID) of the user whose cards are being requested.
   * @return ResponseEntity containing a List of PaymentCardDto (userId, number, holder, expiration
   *     date, active).
   */
  ResponseEntity<List<PaymentCardDto>> getCardByUserId(UUID userId);

  /**
   * Updates an existing payment card's details by its ID. URL: /api/v1/cards/{cardId} with PUT
   * method.
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role. Input data is validated
   * automatically.
   *
   * @param cardId the unique identifier (UUID) of the payment card to update.
   * @param paymentCardDto containing updated card details.
   * @return ResponseEntity containing the updated PaymentCardDto (userId, number, holder,
   *     expiration date, active).
   */
  ResponseEntity<PaymentCardDto> updateCard(UUID cardId, PaymentCardDto paymentCardDto);

  /**
   * Activates a payment card by its ID, making it valid for usage. URL:
   * /api/v1/cards/{cardId}/activation with PATCH method.
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param cardId the unique identifier (UUID) of the payment card to activate.
   * @return ResponseEntity with HTTP status 204 (No Content).
   */
  ResponseEntity<Void> activateCard(UUID cardId);

  /**
   * Deactivates a payment card by its ID, suspending its usage. URL:
   * /api/v1/cards/{cardId}/deactivation with PATCH method.
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param cardId the unique identifier (UUID) of the payment card to deactivate.
   * @return ResponseEntity with HTTP status 204 (No Content).
   */
  ResponseEntity<Void> deactivateCard(UUID cardId);

  /**
   * Deletes a payment card from the system by its ID. URL: /api/v1/cards/{cardId} with DELETE
   * method.
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param cardId the unique identifier (UUID) of the payment card to delete.
   * @return ResponseEntity with HTTP status 204 (No Content).
   */
  ResponseEntity<Void> deleteCard(UUID cardId);
}
