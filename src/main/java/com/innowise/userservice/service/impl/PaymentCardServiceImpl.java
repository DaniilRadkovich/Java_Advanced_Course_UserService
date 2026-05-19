package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.CardLimitException;
import com.innowise.userservice.exception.EntityNotFoundException;
import com.innowise.userservice.exception.EntityValidationException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.specification.PaymentCardSpecification;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {

  private final PaymentCardRepository paymentCardRepository;
  private final PaymentCardMapper paymentCardMapper;
  private final UserService userService;
  private final UserMapper userMapper;

  @Override
  public PaymentCardDto createCard(UUID userId, PaymentCardDto paymentCardDto) {
    if (userService.getActiveCardCount(userId) >= 5) {
      throw new CardLimitException();
    }
    if (paymentCardRepository.findByNumber(paymentCardDto.getNumber()).isPresent()) {
      throw new EntityValidationException("Card with this number already exists!");
    }
    User user = userMapper.toEntity(userService.getUserById(userId));
    PaymentCard paymentCard = paymentCardMapper.toEntity(paymentCardDto);
    paymentCard.setUser(user);
    paymentCard.setActive(true);
    PaymentCard savedPaymentCard = paymentCardRepository.save(paymentCard);

    return paymentCardMapper.toDto(savedPaymentCard);
  }

  @Override
  public PaymentCardDto getCardByCardId(UUID cardId) {
    PaymentCard paymentCard =
        paymentCardRepository.findById(cardId).orElseThrow(EntityNotFoundException::new);
    return paymentCardMapper.toDto(paymentCard);
  }

  @Override
  public Page<PaymentCardDto> getAllCards(String holder, Pageable pageable) {
    Specification<PaymentCard> specification =
        Specification.allOf(PaymentCardSpecification.hasHolder(holder));

    Page<PaymentCard> paymentCards = paymentCardRepository.findAll(specification, pageable);
    return paymentCards.map(paymentCardMapper::toDto);
  }

  @Override
  public List<PaymentCardDto> getCardsByUserId(UUID userId) {
    List<PaymentCard> paymentCards = paymentCardRepository.findPaymentCardByUserId(userId);
    return paymentCards.stream().map(paymentCardMapper::toDto).toList();
  }

  @Override
  @Transactional
  public PaymentCardDto updateCardByCardId(UUID cardId, PaymentCardDto paymentCardDto) {
    PaymentCard paymentCard =
        paymentCardRepository.findById(cardId).orElseThrow(EntityNotFoundException::new);

    paymentCard.setNumber(paymentCardDto.getNumber());
    paymentCard.setHolder(paymentCardDto.getHolder());
    paymentCard.setExpirationDate(paymentCardDto.getExpirationDate());

    paymentCardMapper.updateEntityFromDto(paymentCardDto, paymentCard);
    return paymentCardMapper.toDto(paymentCard);
  }

  @Override
  public void activateCard(UUID cardId) {
    PaymentCard paymentCard =
        paymentCardRepository
            .findById(cardId)
            .orElseThrow(
                () -> new EntityNotFoundException("Card with id: " + cardId + " not found!"));
    paymentCard.setActive(true);
    paymentCardRepository.save(paymentCard);
  }

  @Override
  public void deactivateCard(UUID cardId) {
    PaymentCard paymentCard =
        paymentCardRepository
            .findById(cardId)
            .orElseThrow(
                () -> new EntityNotFoundException("Card with id: " + cardId + " not found!"));
    paymentCard.setActive(false);
    paymentCardRepository.save(paymentCard);
  }

  @Override
  public void deleCardById(UUID cardId) {
    if (!paymentCardRepository.existsById(cardId)) {
      throw new EntityNotFoundException("Card with id: " + cardId + " not found!");
    }
    paymentCardRepository.deleteById(cardId);
  }
}
