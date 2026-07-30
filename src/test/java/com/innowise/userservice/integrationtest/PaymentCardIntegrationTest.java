package com.innowise.userservice.integrationtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.BaseIntegrationTest;
import com.innowise.userservice.model.dto.PaymentCardCreateRequest;
import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PaymentCardIntegrationTest extends BaseIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private PaymentCardRepository paymentCardRepository;

  @Autowired private UserRepository userRepository;

  private User savedUser;

  @BeforeEach
  void setUp() {
    paymentCardRepository.deleteAll();
    userRepository.deleteAll();

    User user =
        User.builder()
            .name("John")
            .surname("Wick")
            .birthDate(LocalDate.of(1970, Month.JUNE, 6))
            .email("john_wick@mail.com")
            .active(true)
            .build();

    savedUser = userRepository.save(user);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void should_success_createPaymentCard() throws Exception {
    PaymentCardCreateRequest request =
        PaymentCardCreateRequest.builder()
            .number("0000444400004444")
            .holder("John Wick")
            .expirationDate(LocalDate.of(2040, Month.DECEMBER, 12))
            .build();

    String response =
        mockMvc
            .perform(
                post("/api/v1/cards/{userId}", savedUser.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    PaymentCardDto result =
        objectMapper.readValue(response, PaymentCardDto.class);

    assertThat(result.getId()).isNotNull();
    assertThat(result.getNumber()).isEqualTo("0000444400004444");
    assertThat(result.getHolder()).isEqualTo("John Wick");

    PaymentCard savedCard =
        paymentCardRepository.findById(result.getId()).orElseThrow();

    assertThat(savedCard.getHolder()).isEqualTo("John Wick");
    assertThat(savedCard.getUser().getId()).isEqualTo(savedUser.getId());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void should_success_getCardById() throws Exception {
    PaymentCard card =
        PaymentCard.builder()
            .number("0000444400004444")
            .holder("John Wick")
            .expirationDate(LocalDate.of(2030, Month.JANUARY, 1))
            .active(true)
            .user(savedUser)
            .build();

    PaymentCard savedCard = paymentCardRepository.save(card);

    String response =
        mockMvc
            .perform(get("/api/v1/cards/{cardId}", savedCard.getId()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    PaymentCardDto result =
        objectMapper.readValue(response, PaymentCardDto.class);

    assertThat(result.getId()).isEqualTo(savedCard.getId());
    assertThat(result.getHolder()).isEqualTo("John Wick");
    assertThat(result.getNumber()).isEqualTo("0000444400004444");
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void should_success_getAllCards() throws Exception {
    PaymentCard card1 =
        PaymentCard.builder()
            .number("0000444400004444")
            .holder("John Wick")
            .expirationDate(LocalDate.of(2030, Month.JANUARY, 1))
            .active(true)
            .user(savedUser)
            .build();

    PaymentCard card2 =
        PaymentCard.builder()
            .number("0000444400004444")
            .holder("LISA CUDDY")
            .expirationDate(LocalDate.of(2031, Month.JANUARY, 1))
            .active(true)
            .user(savedUser)
            .build();

    paymentCardRepository.save(card1);
    paymentCardRepository.save(card2);

    String response =
        mockMvc
            .perform(
                get("/api/v1/cards")
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    JsonNode jsonNode = objectMapper.readTree(response);

    assertThat(jsonNode.get("content").size()).isEqualTo(2);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void should_success_getCardsByUserId() throws Exception {
    PaymentCard card1 =
        PaymentCard.builder()
            .number("0000444400004444")
            .holder("John Wick")
            .expirationDate(LocalDate.of(2030, Month.JANUARY, 1))
            .active(true)
            .user(savedUser)
            .build();

    PaymentCard card2 =
        PaymentCard.builder()
            .number("0000444400004445")
            .holder("John Wick")
            .expirationDate(LocalDate.of(2031, Month.JANUARY, 1))
            .active(true)
            .user(savedUser)
            .build();

    paymentCardRepository.save(card1);
    paymentCardRepository.save(card2);

    String response =
        mockMvc
            .perform(get("/api/v1/cards/user/{userId}", savedUser.getId()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    JsonNode jsonNode = objectMapper.readTree(response);

    assertThat(jsonNode.size()).isEqualTo(2);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void should_success_updateCard() throws Exception {
    PaymentCard card =
        PaymentCard.builder()
            .number("0000444400004444")
            .holder("OLD HOLDER")
            .expirationDate(LocalDate.of(2030, Month.JANUARY, 1))
            .active(true)
            .user(savedUser)
            .build();

    PaymentCard savedCard = paymentCardRepository.save(card);

    PaymentCardDto request =
        PaymentCardDto.builder()
            .number("0000444400005555")
            .holder("NEW HOLDER")
            .expirationDate(LocalDate.of(2035, Month.JANUARY, 1))
            .userId(savedUser.getId())
            .build();

    mockMvc
        .perform(
            put("/api/v1/cards/{cardId}", savedCard.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    PaymentCard updatedCard =
        paymentCardRepository.findById(savedCard.getId()).orElseThrow();

    assertThat(updatedCard.getHolder()).isEqualTo("NEW HOLDER");
    assertThat(updatedCard.getNumber()).isEqualTo("0000444400005555");
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void should_success_activateCard() throws Exception {
    PaymentCard card =
        PaymentCard.builder()
            .number("0000444400004444")
            .holder("John Wick")
            .expirationDate(LocalDate.of(2030, Month.JANUARY, 1))
            .active(false)
            .user(savedUser)
            .build();

    PaymentCard savedCard = paymentCardRepository.save(card);

    mockMvc
        .perform(patch("/api/v1/cards/{cardId}/activation", savedCard.getId()))
        .andExpect(status().isNoContent());

    PaymentCard activatedCard =
        paymentCardRepository.findById(savedCard.getId()).orElseThrow();

    assertThat(activatedCard.isActive()).isTrue();
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void should_success_deactivateCard() throws Exception {
    PaymentCard card =
        PaymentCard.builder()
            .number("0000444400004444")
            .holder("John Wick")
            .expirationDate(LocalDate.of(2030, Month.JANUARY, 1))
            .active(true)
            .user(savedUser)
            .build();

    PaymentCard savedCard = paymentCardRepository.save(card);

    mockMvc
        .perform(patch("/api/v1/cards/{cardId}/deactivation", savedCard.getId()))
        .andExpect(status().isNoContent());

    PaymentCard deactivatedCard =
        paymentCardRepository.findById(savedCard.getId()).orElseThrow();

    assertThat(deactivatedCard.isActive()).isFalse();
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void should_success_deleteCard() throws Exception {
    PaymentCard card =
        PaymentCard.builder()
            .number("0000444400004444")
            .holder("John Wick")
            .expirationDate(LocalDate.of(2030, Month.JANUARY, 1))
            .active(true)
            .user(savedUser)
            .build();

    PaymentCard savedCard = paymentCardRepository.save(card);

    mockMvc
        .perform(delete("/api/v1/cards/{cardId}", savedCard.getId()))
        .andExpect(status().isNoContent());

    boolean exists =
        paymentCardRepository.existsById(savedCard.getId());

    assertThat(exists).isFalse();
  }
}
