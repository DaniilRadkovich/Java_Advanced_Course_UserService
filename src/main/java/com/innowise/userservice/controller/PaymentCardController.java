package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.PaymentCardCreateRequest;
import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing payment cards in the application. Provides endpoints for creating,
 * finding, updating, activating/deactivating the status of payment card and deleting.
 */
@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class PaymentCardController {

  private final PaymentCardService paymentCardService;

  /**
   * Creates a new payment card linked to a specific user.
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
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{userId}")
  public ResponseEntity<PaymentCardDto> createPaymentCard(
      @PathVariable UUID userId, @Valid @RequestBody PaymentCardCreateRequest request) {
    PaymentCardDto createdCard =
        paymentCardService.createCard(
            userId,
            PaymentCardDto.builder()
                .id(request.getId())
                .userId(userId)
                .number(request.getNumber())
                .holder(request.getHolder())
                .expirationDate(request.getExpirationDate())
                .build());
    return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
  }

  /**
   * Finds a payment card by its cardId.
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param cardId the unique identifier (UUID) of the payment card.
   * @return ResponseEntity containing the found PaymentCardDto (userId, number, holder, expiration
   *     date, active).
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{cardId}")
  public ResponseEntity<PaymentCardDto> getCardById(@PathVariable UUID cardId) {
    PaymentCardDto paymentCardDto = paymentCardService.getCardByCardId(cardId);
    return ResponseEntity.ok(paymentCardDto);
  }

  /**
   * Finds a paginated list of all payment cards with optional filtering by holder name, owner name,
   * or owner surname.
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
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<Page<PaymentCardDto>> getAllCards(
      @RequestParam(required = false) String holder,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String surname,
      Pageable pageable) {
    Page<PaymentCardDto> cards = paymentCardService.getAllCards(holder, name, surname, pageable);
    return ResponseEntity.ok(cards);
  }

  /**
   * Finds all payment cards associated with a specific user ID.
   *
   * <p>Access is allowed for users with the 'ADMIN' role or for the owners of the cards matching
   * the requested user ID.
   *
   * @param userId the unique identifier (UUID) of the user whose cards are being requested.
   * @return ResponseEntity containing a List of PaymentCardDto (userId, number, holder, expiration
   *     date, active).
   */
  @PreAuthorize("hasRole('ADMIN') or authentication.principal == #userId")
  @GetMapping("/user/{userId}")
  public ResponseEntity<List<PaymentCardDto>> getCardByUserId(@PathVariable UUID userId) {
    List<PaymentCardDto> cards = paymentCardService.getCardsByUserId(userId);
    return ResponseEntity.ok(cards);
  }

  /**
   * Updates an existing payment card's details by its ID.
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role. Input data is validated
   * automatically.
   *
   * @param cardId the unique identifier (UUID) of the payment card to update.
   * @param paymentCardDto containing updated card details.
   * @return ResponseEntity containing the updated PaymentCardDto (userId, number, holder,
   *     expiration date, active).
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{cardId}")
  public ResponseEntity<PaymentCardDto> updateCard(
      @PathVariable UUID cardId, @Valid @RequestBody PaymentCardDto paymentCardDto) {
    paymentCardDto.setId(cardId);
    PaymentCardDto updatedPaymentCardDto =
        paymentCardService.updateCardByCardId(cardId, paymentCardDto);
    return ResponseEntity.ok(updatedPaymentCardDto);
  }

  /**
   * Activates a payment card by its ID, making it valid for usage.
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param cardId the unique identifier (UUID) of the payment card to activate.
   * @return ResponseEntity with HTTP status 204 (No Content).
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{cardId}/activation")
  public ResponseEntity<Void> activateCard(@PathVariable UUID cardId) {
    paymentCardService.activateCard(cardId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Deactivates a payment card by its ID, suspending its usage.
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param cardId the unique identifier (UUID) of the payment card to deactivate.
   * @return ResponseEntity with HTTP status 204 (No Content).
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{cardId}/deactivation")
  public ResponseEntity<Void> deactivateCard(@PathVariable UUID cardId) {
    paymentCardService.deactivateCard(cardId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Deletes a payment card from the system by its ID.
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param cardId the unique identifier (UUID) of the payment card to delete.
   * @return ResponseEntity with HTTP status 204 (No Content).
   */
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{cardId}")
  public ResponseEntity<Void> deleteCard(@PathVariable UUID cardId) {
    paymentCardService.deleteCardById(cardId);
    return ResponseEntity.noContent().build();
  }
}
