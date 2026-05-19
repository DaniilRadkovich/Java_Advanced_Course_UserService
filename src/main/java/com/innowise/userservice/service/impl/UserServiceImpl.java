package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.EntityNotFoundException;
import com.innowise.userservice.exception.EntityValidationException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.specification.UserSpecification;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private static final String USER_NOT_FOUND = "User not found! id: ";

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  public UserDto createUser(UserDto userDto) {
    if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
      throw new EntityValidationException(
          "User with email: " + userDto.getEmail() + " is already exist!");
    }

    User user = userMapper.toEntity(userDto);
    user.setActive(true);
    User savedUser = userRepository.save(user);
    return userMapper.toDto(savedUser);
  }

  @Override
  public UserDto getUserById(UUID id) {
    User user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    return userMapper.toDto(user);
  }

  @Override
  public Page<UserDto> getAllUsers(String name, String surname, Pageable pageable) {
    Specification<User> specification =
        Specification.allOf(UserSpecification.hasName(name), UserSpecification.hasSurname(surname));

    Page<User> users = userRepository.findAll(specification, pageable);
    return users.map(userMapper::toDto);
  }

  @Override
  @Transactional
  public UserDto updateUserById(UUID userId, UserDto userDto) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND + userDto.getId()));

    userMapper.updateEntityFromDto(userDto, user);
    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public void activateUser(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND + userId));

    user.setActive(true);
    userRepository.save(user);
  }

  @Override
  @Transactional
  public void deactivateUser(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND + userId));

    user.setActive(false);
    userRepository.save(user);
  }

  @Override
  public int getActiveCardCount(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw new EntityNotFoundException(USER_NOT_FOUND + userId);
    }
    return userRepository.getActiveCardCount(userId);
  }

  @Override
  public void deleteUserById(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw new EntityNotFoundException(USER_NOT_FOUND + userId);
    }
    userRepository.deleteById(userId);
  }
}
