package com.innowise.userservice.integrationtest;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.innowise.userservice.BaseIntegrationTest;
import com.innowise.userservice.model.dto.UserCreateRequest;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "INTERNAL_KEY=HGVklnjnwefebHVVjjnweklBJKbwkjbBHJhjbjhbwwf")
@AutoConfigureMockMvc
class UserIntegrationTest extends BaseIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void should_success_createUser() throws Exception {
    UserCreateRequest request = UserCreateRequest.builder()
            .name("Sasha")
            .surname("Petrov")
            .birthDate(LocalDate.of(1991, Month.FEBRUARY, 2))
            .email("sanchez.petrov@mail.com")
            .build();

    String response = mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    UserDto responseDto = objectMapper.readValue(response, UserDto.class);

    assertThat(responseDto.getId()).isNotNull();
    assertThat(responseDto.getName()).isEqualTo("Sasha");
    assertThat(responseDto.getSurname()).isEqualTo("Petrov");
    assertThat(responseDto.getEmail()).isEqualTo("sanchez.petrov@mail.com");
    assertThat(responseDto.isActive()).isTrue();

    User savedUser = userRepository.findById(responseDto.getId()).orElseThrow();

    assertThat(savedUser.getId()).isNotNull();
    assertThat(savedUser.getName()).isEqualTo("Sasha");
    assertThat(savedUser.getSurname()).isEqualTo("Petrov");
    assertThat(savedUser.getBirthDate()).isEqualTo(LocalDate.of(1991, Month.FEBRUARY, 2));
    assertThat(savedUser.getEmail()).isEqualTo("sanchez.petrov@mail.com");
    assertThat(savedUser.isActive()).isTrue();
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void should_success_getUserById() throws Exception {
    User user = User.builder()
            .name("Lisa")
            .surname("Cuddy")
            .birthDate(LocalDate.of(1980, Month.MARCH, 11))
            .email("lisa@mail.com")
            .active(true)
            .build();

    User savedUser = userRepository.save(user);
    UUID id = savedUser.getId();

    String response = mockMvc
            .perform(get("/api/v1/users/{id}", id))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UserDto result = objectMapper.readValue(response, UserDto.class);

    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getName()).isEqualTo("Lisa");
    assertThat(result.getSurname()).isEqualTo("Cuddy");
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void should_success_getAllUsers() throws Exception {
    User user1 = User.builder()
            .name("Sasha")
            .surname("Petrov")
            .birthDate(LocalDate.of(1991, Month.FEBRUARY, 2))
            .email("sanchez.petrov@mail.com")
            .active(true)
            .build();

    User user2 = User.builder()
            .name("Lisa")
            .surname("Cuddy")
            .birthDate(LocalDate.of(1980, Month.MARCH, 11))
            .email("lisa@mail.com")
            .active(true)
            .build();

    userRepository.save(user1);
    userRepository.save(user2);

    String response = mockMvc
            .perform(get("/api/v1/users").param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    JsonNode jsonNode = objectMapper.readTree(response);

    assertThat(jsonNode.get("content").size()).isEqualTo(2);
    assertThat(jsonNode.get("content").get(0).get("id").asText()).isNotBlank();
    assertThat(jsonNode.get("content").get(0).get("name").asText()).isIn("Sasha", "Lisa");
    assertThat(jsonNode.get("content").get(1).get("name").asText()).isIn("Sasha", "Lisa");
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void should_success_updateUser() throws Exception {
    User user = User.builder()
            .name("oldName")
            .surname("oldSurname")
            .birthDate(LocalDate.of(1980, Month.APRIL, 4))
            .email("old@mail.com")
            .active(true)
            .build();

    User savedUser = userRepository.save(user);
    UUID id = savedUser.getId();

    UserDto updateRequest = UserDto.builder()
            .name("newName")
            .surname("newSurname")
            .birthDate(LocalDate.of(2000, Month.MAY, 5))
            .email("new@mail.com")
            .build();

    mockMvc.perform(put("/api/v1/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk());

    User updatedUser = userRepository.findById(id).orElseThrow();

    assertThat(updatedUser.getName()).isEqualTo("newName");
    assertThat(updatedUser.getSurname()).isEqualTo("newSurname");
    assertThat(updatedUser.getEmail()).isEqualTo("new@mail.com");
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void should_success_activateUser() throws Exception {
    User user = User.builder()
            .name("Not active")
            .surname("User")
            .birthDate(LocalDate.of(2000, Month.JANUARY, 1))
            .email("test@mail.com")
            .active(false)
            .build();

    User savedUser = userRepository.save(user);
    UUID id = savedUser.getId();

    mockMvc.perform(patch("/api/v1/users/{id}/activation", id)).andExpect(status().isNoContent());

    User activatedUser = userRepository.findById(id).orElseThrow();

    assertThat(activatedUser.isActive()).isTrue();
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void should_success_deactivateUser() throws Exception {
    User user = User.builder()
            .name("active")
            .surname("User")
            .birthDate(LocalDate.of(2000, Month.JANUARY, 1))
            .email("test@mail.com")
            .active(true)
            .build();

    User savedUser = userRepository.save(user);
    UUID id = savedUser.getId();

    mockMvc.perform(patch("/api/v1/users/{id}/deactivation", id)).andExpect(status().isNoContent());

    User deactivatedUser = userRepository.findById(id).orElseThrow();

    assertThat(deactivatedUser.isActive()).isFalse();
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void should_success_deleteUser() throws Exception {
    User user = User.builder()
            .name("nameToDelete")
            .surname("surnameToDelete")
            .birthDate(LocalDate.of(2000, Month.JANUARY, 1))
            .email("delete@mail.com")
            .active(true)
            .build();

    User savedUser = userRepository.save(user);
    UUID id = savedUser.getId();

    mockMvc.perform(delete("/api/v1/users/{id}", id)).andExpect(status().isNoContent());

    boolean exists = userRepository.existsById(id);

    assertThat(exists).isFalse();
  }

  @Test
  void should_success_createUserInternal() throws Exception {
    UserCreateRequest request = UserCreateRequest.builder()
        .name("Sasha")
        .surname("Petrov")
        .birthDate(LocalDate.of(1991, Month.OCTOBER, 12))
        .email("sasha.petrov@mail.com")
        .build();

    String response = mockMvc.perform(post("/api/v1/users/internal")
            .header("X-Internal-Key", "HGVklnjnwefebHVVjjnweklBJKbwkjbBHJhjbjhbwwf")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    UserDto responseDto = objectMapper.readValue(response, UserDto.class);

    assertThat(responseDto.getId()).isNotNull();
    assertThat(responseDto.getName()).isEqualTo("Sasha");
    assertThat(responseDto.getSurname()).isEqualTo("Petrov");
    assertThat(responseDto.getEmail()).isEqualTo("sasha.petrov@mail.com");
    assertThat(responseDto.isActive()).isTrue();

    User savedUser = userRepository.findById(responseDto.getId()).orElseThrow();

    assertThat(savedUser.getId()).isEqualTo(responseDto.getId());
    assertThat(savedUser.getName()).isEqualTo("Sasha");
    assertThat(savedUser.getSurname()).isEqualTo("Petrov");
    assertThat(savedUser.getBirthDate()).isEqualTo(LocalDate.of(1991, Month.OCTOBER, 12));
    assertThat(savedUser.getEmail()).isEqualTo("sasha.petrov@mail.com");
    assertThat(savedUser.isActive()).isTrue();
  }

  @Test
  void should_success_deleteUserInternal() throws Exception {
    User user = User.builder()
        .name("Vasya")
        .surname("Pupkin")
        .birthDate(LocalDate.of(1991, Month.NOVEMBER, 1))
        .email("pupkin@mail.com")
        .active(true)
        .build();

    User savedUser = userRepository.save(user);
    UUID id = savedUser.getId();

    mockMvc.perform(delete("/api/v1/users/internal/{id}", id)
        .header("X-Internal-Key", "HGVklnjnwefebHVVjjnweklBJKbwkjbBHJhjbjhbwwf"))
        .andDo(print())
        .andExpect(status().isNoContent());

    boolean exists = userRepository.existsById(id);
    assertThat(exists).isFalse();
  }
}
