package com.innowise.userservice.controller.impl;

import com.innowise.userservice.controller.UserController;
import com.innowise.userservice.model.dto.UserCreateRequest;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {

  private final UserService userService;

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping()
  public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequest request) {
    UserDto savedUserDto =
        userService.createUser(
            UserDto.builder()
                .id(request.getId())
                .name(request.getName())
                .surname(request.getSurname())
                .birthDate(request.getBirthDate())
                .email(request.getEmail())
                .active(true)
                .build());
    return ResponseEntity.status(HttpStatus.CREATED).body(savedUserDto);
  }

  @PreAuthorize("hasRole('ADMIN') or authentication.principal == #id")
  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
    UserDto user = userService.getUserById(id);
    return ResponseEntity.ok(user);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<Page<UserDto>> getAllUsers(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String surname,
      Pageable pageable) {
    Page<UserDto> users = userService.getAllUsers(name, surname, pageable);
    return ResponseEntity.ok(users);
  }

  @PreAuthorize("hasRole('ADMIN') or authentication.principal == #id")
  @PutMapping("/{id}")
  public ResponseEntity<UserDto> updateUser(
      @PathVariable UUID id, @Valid @RequestBody UserDto userDto) {
    userDto.setId(id);
    UserDto updatedUserDto = userService.updateUserById(id, userDto);
    return ResponseEntity.ok(updatedUserDto);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/activation")
  public ResponseEntity<Void> activateUser(@PathVariable UUID id) {
    userService.activateUser(id);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/deactivation")
  public ResponseEntity<Void> deactivateUser(@PathVariable UUID id) {
    userService.deactivateUser(id);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
    userService.deleteUserById(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/internal")
  public ResponseEntity<UserDto> createUserInternal(@Valid @RequestBody UserCreateRequest request) {
    log.info("REQUEST = {}", request);
    UserDto savedUser =
        userService.createUser(
            UserDto.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .birthDate(request.getBirthDate())
                .email(request.getEmail())
                .active(true)
                .build());
    return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
  }

  @DeleteMapping("/internal/{id}")
  public ResponseEntity<Void> deleteUserInternal(@PathVariable UUID id) {
    userService.deleteUserById(id);
    return ResponseEntity.noContent().build();
  }
}
