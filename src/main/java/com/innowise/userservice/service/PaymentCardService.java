package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.PaymentCardDto;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface defining the business contract for managing payment cards. Provides methods for
 * CRUD operations, account status editing, and card aggregation.
 */
public interface PaymentCardService {

  /**
   * Creates a new payment card for a specific user.
   *
   * @param userId the unique identifier (UUID) of the card owner.
   * @param paymentCardDto (userId, number, holder, * expiration date, active) containing card
   *     details to register.
   * @return the saved PaymentCardDto containing generated system fields.
   */
  PaymentCardDto createCard(UUID userId, PaymentCardDto paymentCardDto);

  /**
   * Finds a specific payment card by its unique identifier.
   *
   * @param cardId the unique identifier (UUID) of the payment card.
   * @return the found PaymentCardDto (userId, number, holder, * expiration date, active).
   */
  PaymentCardDto getCardByCardId(UUID cardId);

  /**
   * Finds a filtered and paginated page of payment cards.
   *
   * @param holder optional filter matching the cardholder name.
   * @param name optional filter matching the card owner's first name.
   * @param surname optional filter matching the card owner's surname.
   * @param pageable pagination and sorting parameters.
   * @return a Page of PaymentCardDto (userId, number, holder, expiration date, active) matching the
   *     entered data.
   */
  Page<PaymentCardDto> getAllCards(String holder, String name, String surname, Pageable pageable);

  /**
   * Finds all payment cards associated with a specific user.
   *
   * @param userId the unique identifier (UUID) of the card owner.
   * @return a List of PaymentCardDto (userId, number, holder, expiration date, active) belonging to
   *     the user.
   */
  List<PaymentCardDto> getCardsByUserId(UUID userId);

  /**
   * Updates an existing payment card's details by its ID.
   *
   * @param cardId the unique identifier (UUID) of the card to update.
   * @param paymentCardDto the PaymentCardDto (userId, number, holder, expiration date, active)
   *     containing updated values.
   * @return the updated PaymentCardDto.
   */
  PaymentCardDto updateCardByCardId(UUID cardId, PaymentCardDto paymentCardDto);

  /**
   * Activates a payment card.
   *
   * @param cardId the unique identifier (UUID) of the card to activate.
   */
  void activateCard(UUID cardId);

  /**
   * Deactivates a payment card.
   *
   * @param cardId the unique identifier (UUID) of the card to deactivate.
   */
  void deactivateCard(UUID cardId);

  /**
   * Permanently deletes a payment card from the system.
   *
   * @param cardId the unique identifier (UUID) of the card to delete.
   */
  void deleteCardById(UUID cardId);
}
