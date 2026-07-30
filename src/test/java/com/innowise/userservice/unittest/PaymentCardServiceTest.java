package com.innowise.userservice.unittest;

import com.innowise.userservice.exception.CardLimitException;
import com.innowise.userservice.exception.EntityNotFoundException;
import com.innowise.userservice.exception.EntityValidationException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.service.impl.PaymentCardServiceImpl;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

  @Mock private PaymentCardRepository paymentCardRepository;
  @Mock private PaymentCardMapper paymentCardMapper;
  @Mock private UserService userService;
  @Mock private UserRepository userRepository;
  @Mock private UserMapper userMapper;
  @Mock private CacheManager cacheManager;
  @Mock private Cache cache;

  @InjectMocks private PaymentCardServiceImpl paymentCardService;

  private static final UUID USER_ID = UUID.randomUUID();
  private static final UUID CARD_ID = UUID.randomUUID();

  private PaymentCardDto paymentCardDto;
  private PaymentCard paymentCard;
  private User user;

  @BeforeEach
  void setUp() {
    user =
        User.builder()
            .id(USER_ID)
            .name("test")
            .surname("test")
            .birthDate(LocalDate.of(2000, Month.FEBRUARY, 22))
            .email("test@mail.com")
            .active(true)
            .build();

    paymentCard =
        PaymentCard.builder()
            .id(CARD_ID)
            .user(user)
            .number("4444444444444444")
            .holder("Nick Nack")
            .build();

    paymentCardDto =
        PaymentCardDto.builder()
            .id(CARD_ID)
            .userId(USER_ID)
            .number("4444444444444444")
            .holder("Nick Nack")
            .build();
  }

  @Test
  void should_success_createCard() {
    when(userService.getCardCount(USER_ID)).thenReturn(4);
    when(paymentCardRepository.findByNumber(paymentCardDto.getNumber()))
        .thenReturn(Optional.empty());
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(paymentCardMapper.toEntity(paymentCardDto)).thenReturn(paymentCard);
    when(paymentCardRepository.save(paymentCard)).thenReturn(paymentCard);
    when(paymentCardMapper.toDto(paymentCard)).thenReturn(paymentCardDto);

    PaymentCardDto result = paymentCardService.createCard(USER_ID, paymentCardDto);

    assertThat(result).isNotNull();
    assertThat(paymentCard.isActive()).isTrue();
    verify(paymentCardRepository).save(paymentCard);
  }

  @Test
  void should_throwCardLimitException_createCard() {
    when(userService.getCardCount(USER_ID)).thenReturn(5);

    assertThatThrownBy(() -> paymentCardService.createCard(USER_ID, paymentCardDto))
        .isInstanceOf(CardLimitException.class);
    verifyNoInteractions(paymentCardRepository, paymentCardMapper, userMapper);
  }

  @Test
  void should_throwEntityValidationException_createCard() {
    when(userService.getCardCount(USER_ID)).thenReturn(3);
    when(paymentCardRepository.findByNumber(paymentCardDto.getNumber()))
        .thenReturn(Optional.of(paymentCard));

    assertThatThrownBy(() -> paymentCardService.createCard(USER_ID, paymentCardDto))
        .isInstanceOf(EntityValidationException.class)
        .hasMessage("Card with this number already exists!");
    verifyNoMoreInteractions(paymentCardRepository);
  }

  @Test
  void should_success_getCardByCardId() {
    when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(paymentCard));
    when(paymentCardMapper.toDto(paymentCard)).thenReturn(paymentCardDto);

    PaymentCardDto result = paymentCardService.getCardByCardId(CARD_ID);

    assertThat(result).isEqualTo(paymentCardDto);
  }

  @Test
  void should_throwEntityNotFoundException_getCardByCardId() {
    when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentCardService.getCardByCardId(CARD_ID))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void should_success_getAllCards() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<PaymentCard> page = new PageImpl<>(List.of(paymentCard));

    when(paymentCardRepository.findAll(Mockito.<Specification<PaymentCard>>any(), eq(pageable)))
        .thenReturn(page);
    when(paymentCardMapper.toDto(paymentCard)).thenReturn(paymentCardDto);

    Page<PaymentCardDto> result =
        paymentCardService.getAllCards("Nick Nack", "test", "test", pageable);

    assertThat(result).isNotEmpty();
    assertThat(result.getContent().getFirst()).isEqualTo(paymentCardDto);
  }

  @Test
  void should_success_getCardsByUserId() {
    when(paymentCardRepository.findPaymentCardByUserId(USER_ID)).thenReturn(List.of(paymentCard));
    when(paymentCardMapper.toDto(paymentCard)).thenReturn(paymentCardDto);

    List<PaymentCardDto> result = paymentCardService.getCardsByUserId(USER_ID);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst()).isEqualTo(paymentCardDto);
  }

  @Test
  void should_success_updateCardByCardId() {
    when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(paymentCard));
    when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(paymentCard);
    when(paymentCardMapper.toDto(any(PaymentCard.class))).thenReturn(paymentCardDto);

    PaymentCardDto result = paymentCardService.updateCardByCardId(CARD_ID, paymentCardDto);

    assertThat(result).isEqualTo(paymentCardDto);
    verify(paymentCardMapper).updateEntityFromDto(paymentCardDto, paymentCard);
  }

  private void should_success_mockCache() {
    when(cacheManager.getCache("userCache")).thenReturn(cache);
  }

  @Test
  void should_success_activateCard() {
    should_success_mockCache();
    when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(paymentCard));

    paymentCardService.activateCard(CARD_ID);

    assertThat(paymentCard.isActive()).isTrue();
    verify(paymentCardRepository).saveAndFlush(paymentCard);
    verify(cache).evict(USER_ID);
  }

  @Test
  void should_success_deactivateCard() {
    should_success_mockCache();
    when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(paymentCard));

    paymentCardService.deactivateCard(CARD_ID);

    assertThat(paymentCard.isActive()).isFalse();
    verify(paymentCardRepository).saveAndFlush(paymentCard);
    verify(cache).evict(USER_ID);
  }

  @Test
  void should_success_deleteCardById() {
    should_success_mockCache();
    when(paymentCardRepository.existsById(CARD_ID)).thenReturn(true);
    when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(paymentCard));

    paymentCardService.deleteCardById(CARD_ID);

    verify(paymentCardRepository).delete(paymentCard);
    verify(paymentCardRepository).flush();
    verify(cache).evict(USER_ID);
  }

  @Test
  void should_throwEntityNotFoundException_deleteCardById() {
    when(paymentCardRepository.existsById(CARD_ID)).thenReturn(false);

    assertThatThrownBy(() -> paymentCardService.deleteCardById(CARD_ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Card not found! id: " + CARD_ID);
    verify(paymentCardRepository, never()).delete(any());
  }
}
