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
public class PaymentCardController {

  private final PaymentCardService paymentCardService;

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

  @GetMapping("/{cardId}")
  public ResponseEntity<PaymentCardDto> getCardById(@PathVariable UUID cardId) {
    PaymentCardDto paymentCardDto = paymentCardService.getCardByCardId(cardId);
    return ResponseEntity.ok(paymentCardDto);
  }

  @GetMapping
  public ResponseEntity<Page<PaymentCardDto>> getAllCards(
      @RequestParam(required = false) String holder, Pageable pageable) {
    Page<PaymentCardDto> cards = paymentCardService.getAllCards(holder, pageable);
    return ResponseEntity.ok(cards);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<PaymentCardDto>> getCardByUserId(@PathVariable UUID userId) {
    List<PaymentCardDto> cards = paymentCardService.getCardsByUserId(userId);
    return ResponseEntity.ok(cards);
  }

  @PutMapping("/{cardId}")
  public ResponseEntity<PaymentCardDto> updateCard(
      @PathVariable UUID cardId, @Valid @RequestBody PaymentCardDto paymentCardDto) {
    paymentCardDto.setId(cardId);
    paymentCardDto.setActive(true);
    PaymentCardDto updatedPaymentCardDto =
        paymentCardService.updateCardByCardId(cardId, paymentCardDto);
    return ResponseEntity.ok(updatedPaymentCardDto);
  }

  @PatchMapping("/activate/{cardId}")
  public ResponseEntity<PaymentCardDto> activateCard(@PathVariable UUID cardId) {
    paymentCardService.activateCard(cardId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/deactivate/{cardId}")
  public ResponseEntity<PaymentCardDto> deactivateCard(@PathVariable UUID cardId) {
    paymentCardService.deactivateCard(cardId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{cardId}")
  public ResponseEntity<PaymentCardDto> deleteCard(@PathVariable UUID cardId) {
    paymentCardService.deleteCardById(cardId);
    return ResponseEntity.noContent().build();
  }
}
