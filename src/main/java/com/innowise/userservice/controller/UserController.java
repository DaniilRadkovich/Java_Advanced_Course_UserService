package com.innowise.userservice.controller;

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

/**
 * REST controller for managing users in the application. Provides endpoints for creating, finding,
 * updating, activating/deactivating the status of user and deleting.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  /**
   * Creates a new user in the application.
   *
   * <p>This endpoint validates the input data and throws exceptions if the validation fails. It
   * also checks the user's authorities and denies access if the requester is not an ADMIN.
   *
   * @param request UserCreateRequest which contains the following data: userId,name, surname,
   *     birthDate, email.
   * @return UserDto wrapped in ResponseEntity which contains the following data: userId, name,
   *     surname, birthDate, email, active, paymentCards.
   */
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

  /**
   * Finds a user in the application database by their ID.
   *
   * <p>Access is allowed for users with the 'ADMIN' role or for the users themselves matching the
   * requested ID.
   *
   * @param id the unique identifier (UUID) of the user to find.
   * @return UserDto wrapped in ResponseEntity which contains the following data: userId, name,
   *     surname, birthDate, email, active, paymentCards if found.
   */
  @PreAuthorize("hasRole('ADMIN') or authentication.principal == #id")
  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
    UserDto user = userService.getUserById(id);
    return ResponseEntity.ok(user);
  }

  /**
   * Finds a paginated list of all users, with optional filtering by name and surname.
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param name optional filter for the user's first name.
   * @param surname optional filter for the user's surname.
   * @param pageable pagination and sorting information.
   * @return ResponseEntity containing a Page of UserDto which contains the following data: userId,
   *     name, surname, birthDate, email, active, paymentCards.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<Page<UserDto>> getAllUsers(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String surname,
      Pageable pageable) {
    Page<UserDto> users = userService.getAllUsers(name, surname, pageable);
    return ResponseEntity.ok(users);
  }

  /**
   * Updates an existing user's information by their ID.
   *
   * <p>Access is allowed for users with the 'ADMIN' role or for the users themselves matching the
   * requested ID. Input data is validated automatically.
   *
   * @param id the unique identifier (UUID) of the user to update.
   * @param userDto containing updated user information (userId, name, surname, birthDate, email,
   *     active, paymentCards).
   * @return ResponseEntity containing the updated UserDto which contains the following data:
   *     userId, name, surname, birthDate, email, active, paymentCards.
   */
  @PreAuthorize("hasRole('ADMIN') or authentication.principal == #id")
  @PutMapping("/{id}")
  public ResponseEntity<UserDto> updateUser(
      @PathVariable UUID id, @Valid @RequestBody UserDto userDto) {
    userDto.setId(id);
    UserDto updatedUserDto = userService.updateUserById(id, userDto);
    return ResponseEntity.ok(updatedUserDto);
  }

  /**
   * Activates a user account by their ID.
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param id the unique identifier (UUID) of the user to activate.
   * @return ResponseEntity with HTTP status 204 (No Content).
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/activation")
  public ResponseEntity<Void> activateUser(@PathVariable UUID id) {
    userService.activateUser(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Deactivates a user account by their ID.
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param id the unique identifier (UUID) of the user to deactivate.
   * @return ResponseEntity with HTTP status 204 (No Content).
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/deactivation")
  public ResponseEntity<Void> deactivateUser(@PathVariable UUID id) {
    userService.deactivateUser(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Deletes a user from the database by their ID.
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param id the unique identifier (UUID) of the user to delete.
   * @return ResponseEntity with HTTP status 204 (No Content).
   */
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
    userService.deleteUserById(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Creates a new user in the application from internal sources (AUTH Service).
   *
   * <p>This endpoint validates the input data and throws exceptions if the validation fails. It
   * also checks the user's authorities and denies access if the requester is not an ADMIN.
   *
   * @param request UserCreateRequest which contains the following data: userId,name, surname,
   *     birthDate, email.
   * @return UserDto wrapped in ResponseEntity which contains the following data: userId, name,
   *     surname, birthDate, email, active, paymentCards.
   */
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

  /**
   * Deletes a user while creating a new user in the application from internal sources (AUTH
   * Service) fails.
   *
   * @param id the unique identifier (UUID) of the user to delete.
   * @return ResponseEntity with HTTP status 204 (No Content).
   */
  @DeleteMapping("/internal/{id}")
  public ResponseEntity<Void> deleteUserInternal(@PathVariable UUID id) {
    userService.deleteUserById(id);
    return ResponseEntity.noContent().build();
  }
}
