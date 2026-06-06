package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.UserDto;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

  UserDto createUser(UserDto userDto);

  UserDto getUserById(UUID id);

  Page<UserDto> getAllUsers(String name, String surname, Pageable pageable);

  UserDto updateUserById(UUID userId, UserDto userDto);

  void activateUser(UUID userId);

  void deactivateUser(UUID userId);

  int getCardCount(UUID userId);

  void deleteUserById(UUID userId);
}
