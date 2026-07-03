package com.innowise.userservice.controller.impl;

import com.innowise.userservice.controller.PaymentCardController;
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

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class PaymentCardControllerImpl implements PaymentCardController {

  private final PaymentCardService paymentCardService;

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

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{cardId}")
  public ResponseEntity<PaymentCardDto> getCardById(@PathVariable UUID cardId) {
    PaymentCardDto paymentCardDto = paymentCardService.getCardByCardId(cardId);
    return ResponseEntity.ok(paymentCardDto);
  }

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

  @PreAuthorize("hasRole('ADMIN') or authentication.principal == #userId")
  @GetMapping("/user/{userId}")
  public ResponseEntity<List<PaymentCardDto>> getCardByUserId(@PathVariable UUID userId) {
    List<PaymentCardDto> cards = paymentCardService.getCardsByUserId(userId);
    return ResponseEntity.ok(cards);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{cardId}")
  public ResponseEntity<PaymentCardDto> updateCard(
      @PathVariable UUID cardId, @Valid @RequestBody PaymentCardDto paymentCardDto) {
    paymentCardDto.setId(cardId);
    PaymentCardDto updatedPaymentCardDto =
        paymentCardService.updateCardByCardId(cardId, paymentCardDto);
    return ResponseEntity.ok(updatedPaymentCardDto);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{cardId}/activation")
  public ResponseEntity<Void> activateCard(@PathVariable UUID cardId) {
    paymentCardService.activateCard(cardId);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{cardId}/deactivation")
  public ResponseEntity<Void> deactivateCard(@PathVariable UUID cardId) {
    paymentCardService.deactivateCard(cardId);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{cardId}")
  public ResponseEntity<Void> deleteCard(@PathVariable UUID cardId) {
    paymentCardService.deleteCardById(cardId);
    return ResponseEntity.noContent().build();
  }
}
