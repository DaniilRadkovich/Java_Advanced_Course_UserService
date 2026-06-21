package com.innowise.userservice.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.userservice.exception.EntityNotFoundException;
import com.innowise.userservice.exception.EntityValidationException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.impl.UserServiceImpl;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private static final UUID USER_ID = UUID.randomUUID();

  @Mock private UserRepository userRepository;

  @Mock private UserMapper userMapper;

  @InjectMocks private UserServiceImpl userService;

  private User user;
  private UserDto userDto;

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

    userDto =
        UserDto.builder()
            .id(USER_ID)
            .name("test")
            .surname("test")
            .birthDate(LocalDate.of(2000, Month.FEBRUARY, 22))
            .email("test@mail.com")
            .active(true)
            .build();
  }

  @Test
  void should_success_createUser() {
    when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.empty());
    when(userMapper.toEntity(userDto)).thenReturn(user);
    when(userRepository.save(user)).thenReturn(user);
    when(userMapper.toDto(user)).thenReturn(userDto);
    UserDto createdUser = userService.createUser(userDto);

    assertNotNull(createdUser);
    assertEquals(userDto.getEmail(), createdUser.getEmail());
    assertTrue(user.isActive());
    verify(userRepository).save(user);
  }

  @Test
  void should_throwEntityValidationException_whenEmailAlreadyExists_createUser() {
    String testEmail = "alreadyExist@mail.com";
    userDto.setEmail(testEmail);

    User existingUser = new User();
    existingUser.setId(USER_ID);
    existingUser.setEmail(testEmail);
    existingUser.setName("existingUser");

    when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(existingUser));

    EntityValidationException exception =
        assertThrows(EntityValidationException.class, () -> userService.createUser(userDto));

    assertTrue(exception.getMessage().contains("already exist"));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void should_returnUser_getUserById() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(userMapper.toDto(user)).thenReturn(userDto);

    UserDto foundUser = userService.getUserById(USER_ID);

    assertEquals("test", foundUser.getName());
  }

  @Test
  void should_throwEntityNotFoundException_getUserById() {
    when(userRepository.findById(USER_ID)).thenThrow(new EntityNotFoundException());

    assertThrows(EntityNotFoundException.class, () -> userService.getUserById(USER_ID));
  }

  @Test
  void should_returnPageOfUsers_getAllUsers() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<User> userPage = new PageImpl<>(List.of(user));

    when(userRepository.findAll(Mockito.<Specification<User>>any(), eq(pageable)))
        .thenReturn(userPage);
    when(userMapper.toDto(user)).thenReturn(userDto);

    Page<UserDto> createdPage = userService.getAllUsers("test", "test", pageable);

    assertNotNull(createdPage);
    assertEquals(1, createdPage.getTotalElements());
    assertEquals(userDto.getName(), createdPage.getContent().getFirst().getName());
  }

  @Test
  void should_updateUser_updateUserById() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(userRepository.saveAndFlush(any(User.class))).thenReturn(user);
    when(userMapper.toDto(any(User.class))).thenReturn(userDto);

    UserDto updatedUser = userService.updateUserById(USER_ID, userDto);

    assertNotNull(updatedUser);
    assertEquals(userDto.getId(), updatedUser.getId());
    verify(userRepository).findById(USER_ID);
    verify(userRepository).saveAndFlush(any(User.class));
  }

  @Test
  void should_throwEntityNotFoundException_updateUserById() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> userService.updateUserById(USER_ID, userDto));
  }

  @Test
  void should_success_activateUser() {
    user.setActive(false);
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(userRepository.saveAndFlush(any(User.class))).thenReturn(user);

    userService.activateUser(USER_ID);

    assertTrue(user.isActive());
    verify(userRepository).findById(USER_ID);
    verify(userRepository).saveAndFlush(user);
  }

  @Test
  void should_throwEntityNotFoundException_activateUser() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> userService.activateUser(USER_ID));
  }

  @Test
  void should_success_deactivateUser() {
    user.setActive(true);
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(userRepository.saveAndFlush(any(User.class))).thenReturn(user);

    userService.deactivateUser(USER_ID);

    org.junit.jupiter.api.Assertions.assertFalse(user.isActive());
    verify(userRepository).findById(USER_ID);
    verify(userRepository).saveAndFlush(user);
  }

  @Test
  void should_throwEntityNotFoundException_deactivateUser() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> userService.deactivateUser(USER_ID));
  }

  @Test
  void should_returnCardCount_getActiveCardCount() {
    int expectedCount = 3;
    when(userRepository.existsById(USER_ID)).thenReturn(true);
    when(userRepository.getCardCount(USER_ID)).thenReturn(expectedCount);

    int actualCount = userService.getCardCount(USER_ID);

    assertEquals(expectedCount, actualCount);
    verify(userRepository).existsById(USER_ID);
    verify(userRepository).getCardCount(USER_ID);
  }

  @Test
  void should_throwEntityNotFoundException_getActiveCardCount() {
    when(userRepository.existsById(USER_ID)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> userService.getCardCount(USER_ID));
  }

  @Test
  void should_success_deleteUserById() {
    when(userRepository.existsById(USER_ID)).thenReturn(true);

    userService.deleteUserById(USER_ID);

    verify(userRepository).existsById(USER_ID);
    verify(userRepository).deleteById(USER_ID);
  }

  @Test
  void should_throwEntityNotFoundException_deleteUserById() {
    when(userRepository.existsById(USER_ID)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> userService.deleteUserById(USER_ID));
  }
}
