package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.CardLimitException;
import com.innowise.userservice.exception.EntityNotFoundException;
import com.innowise.userservice.exception.EntityValidationException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.specification.PaymentCardSpecification;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {

  private static final String CARD_NOT_FOUND = "Card not found! id: ";
  private static final String USER_CACHE = "userCache";

  private final PaymentCardRepository paymentCardRepository;
  private final PaymentCardMapper paymentCardMapper;
  private final UserService userService;
  private final UserRepository userRepository;
  private final CacheManager cacheManager;

  @Override
  @CacheEvict(value = "cardCache", key = "#userId")
  @Transactional
  public PaymentCardDto createCard(UUID userId, PaymentCardDto paymentCardDto) {
    if (userService.getCardCount(userId) >= 5) {
      throw new CardLimitException();
    }
    if (paymentCardRepository.findByNumber(paymentCardDto.getNumber()).isPresent()) {
      throw new EntityValidationException("Card with this number already exists!");
    }
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found! id: " + userId));

    PaymentCard paymentCard = paymentCardMapper.toEntity(paymentCardDto);
    paymentCard.setUser(user);
    paymentCard.setActive(true);
    PaymentCard savedPaymentCard = paymentCardRepository.save(paymentCard);
    return paymentCardMapper.toDto(savedPaymentCard);
  }

  @Override
  @Cacheable(value = "cardCache", key = "#cardId")
  @Transactional(readOnly = true)
  public PaymentCardDto getCardByCardId(UUID cardId) {
    PaymentCard paymentCard =
        paymentCardRepository.findById(cardId).orElseThrow(EntityNotFoundException::new);
    return paymentCardMapper.toDto(paymentCard);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PaymentCardDto> getAllCards(
      String holder, String name, String surname, Pageable pageable) {
    Specification<PaymentCard> specification =
        Specification.allOf(
            PaymentCardSpecification.hasHolder(holder),
            PaymentCardSpecification.hasFirstName(name),
            PaymentCardSpecification.hasLastName(surname));

    Page<PaymentCard> paymentCards = paymentCardRepository.findAll(specification, pageable);
    return paymentCards.map(paymentCardMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentCardDto> getCardsByUserId(UUID userId) {
    List<PaymentCard> paymentCards = paymentCardRepository.findPaymentCardByUserId(userId);
    return paymentCards.stream().map(paymentCardMapper::toDto).toList();
  }

  @Override
  @Caching(
      evict = {
        @CacheEvict(value = "userCache", key = "#result.userId"),
        @CacheEvict(value = "cardCache", key = "#cardId")
      })
  @Transactional
  public PaymentCardDto updateCardByCardId(UUID cardId, PaymentCardDto paymentCardDto) {
    PaymentCard paymentCard =
        paymentCardRepository.findById(cardId).orElseThrow(EntityNotFoundException::new);

    paymentCardMapper.updateEntityFromDto(paymentCardDto, paymentCard);
    PaymentCard updatedCard = paymentCardRepository.save(paymentCard);
    return paymentCardMapper.toDto(updatedCard);
  }

  @Override
  @CacheEvict(value = "cardCache", key = "#cardId")
  @Transactional
  public void activateCard(UUID cardId) {
    PaymentCard paymentCard =
        paymentCardRepository
            .findById(cardId)
            .orElseThrow(() -> new EntityNotFoundException(CARD_NOT_FOUND + cardId));
    paymentCard.setActive(true);
    paymentCardRepository.saveAndFlush(paymentCard);

    if (paymentCard.getUser() != null && cacheManager.getCache(USER_CACHE) != null) {
      Objects.requireNonNull(cacheManager.getCache(USER_CACHE))
          .evict(paymentCard.getUser().getId());
    }
  }

  @Override
  @CacheEvict(value = "cardCache", key = "#cardId")
  @Transactional
  public void deactivateCard(UUID cardId) {
    PaymentCard paymentCard =
        paymentCardRepository
            .findById(cardId)
            .orElseThrow(() -> new EntityNotFoundException(CARD_NOT_FOUND + cardId));
    paymentCard.setActive(false);
    paymentCardRepository.saveAndFlush(paymentCard);

    if (paymentCard.getUser() != null && cacheManager.getCache(USER_CACHE) != null) {
      Objects.requireNonNull(cacheManager.getCache(USER_CACHE))
          .evict(paymentCard.getUser().getId());
    }
  }

  @Override
  @CacheEvict(value = "cardCache", key = "#cardId")
  @Transactional
  public void deleteCardById(UUID cardId) {
    if (!paymentCardRepository.existsById(cardId)) {
      throw new EntityNotFoundException(CARD_NOT_FOUND + cardId);
    }
    PaymentCard paymentCard =
        paymentCardRepository
            .findById(cardId)
            .orElseThrow(() -> new EntityNotFoundException(CARD_NOT_FOUND + cardId));

    UUID userId = (paymentCard.getUser() != null) ? paymentCard.getUser().getId() : null;

    paymentCardRepository.delete(paymentCard);
    paymentCardRepository.flush();

    if (userId != null && cacheManager.getCache(USER_CACHE) != null) {
      Objects.requireNonNull(cacheManager.getCache(USER_CACHE)).evict(userId);
    }
  }
}
