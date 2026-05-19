package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.PaymentCardDto;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentCardService {

  PaymentCardDto createCard(UUID userId, PaymentCardDto paymentCardDto);

  PaymentCardDto getCardByCardId(UUID cardId);

  Page<PaymentCardDto> getAllCards(String holder, Pageable pageable);

  List<PaymentCardDto> getCardsByUserId(UUID userId);

  PaymentCardDto updateCardByCardId(UUID cardId, PaymentCardDto paymentCardDto);

  void activateCard(UUID cardId);

  void deactivateCard(UUID cardId);

  void deleCardById(UUID cardId);
}
