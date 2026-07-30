package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.UserCreateRequest;
import com.innowise.userservice.model.dto.UserDto;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

/**
 * User controller interface for managing users in the application. Provides endpoints for creating,
 * finding, updating, activating/deactivating the status of user and deleting. URL prefix:
 * /api/v1/users
 */
public interface UserController {

  /**
   * Creates a new user in the application. URL: /api/v1/users with POST method
   *
   * <p>This endpoint validates the input data and throws exceptions if the validation fails. It
   * also checks the user's authorities and denies access if the requester is not an ADMIN.
   *
   * @param request UserCreateRequest which contains the following data: userId,name, surname,
   *     birthDate, email.
   * @return UserDto wrapped in ResponseEntity which contains the following data: userId, name,
   *     surname, birthDate, email, active, paymentCards.
   */
  ResponseEntity<UserDto> createUser(UserCreateRequest request);

  /**
   * Finds a user in the application database by their ID. URL: /api/v1/users/{id} with GET method
   *
   * <p>Access is allowed for users with the 'ADMIN' role or for the users themselves matching the
   * requested ID.
   *
   * @param id the unique identifier (UUID) of the user to find.
   * @return UserDto wrapped in ResponseEntity which contains the following data: userId, name,
   *     surname, birthDate, email, active, paymentCards if found.
   */
  ResponseEntity<UserDto> getUserById(UUID id);

  /**
   * Finds a paginated list of all users, with optional filtering by name and surname. URL:
   * /api/v1/users with GET method
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param name optional filter for the user's first name.
   * @param surname optional filter for the user's surname.
   * @param pageable pagination and sorting information.
   * @return ResponseEntity containing a Page of UserDto which contains the following data: userId,
   *     name, surname, birthDate, email, active, paymentCards.
   */
  ResponseEntity<Page<UserDto>> getAllUsers(String name, String surname, Pageable pageable);

  /**
   * Updates an existing user's information by their ID. URL: /api/v1/users/{id} with PUT method
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
  ResponseEntity<UserDto> updateUser(UUID id, UserDto userDto);

  /**
   * Activates a user account by their ID. URL: /api/v1/users/{id}/activation with PATCH method
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param id the unique identifier (UUID) of the user to activate.
   * @return ResponseEntity with HTTP status 204 (No Content).
   */
  ResponseEntity<Void> activateUser(UUID id);

  /**
   * Deactivates a user account by their ID. URL: /api/v1/users/{id}/deactivation with PATCH method
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param id the unique identifier (UUID) of the user to deactivate.
   * @return ResponseEntity with HTTP status 204 (No Content).
   */
  ResponseEntity<Void> deactivateUser(UUID id);

  /**
   * Deletes a user from the database by their ID. URL: /api/v1/users/{id} with DELETE method
   *
   * <p>Access is allowed exclusively to users with the 'ADMIN' role.
   *
   * @param id the unique identifier (UUID) of the user to delete.
   * @return ResponseEntity with HTTP status 204 (No Content).
   */
  ResponseEntity<Void> deleteUser(UUID id);

  /**
   * Creates a new user in the application from internal sources (AUTH Service). URL:
   * /api/v1/users/internal with POST method
   *
   * <p>This endpoint validates the input data and throws exceptions if the validation fails. It
   * also checks the user's authorities and denies access if the requester is not an ADMIN.
   *
   * @param request UserCreateRequest which contains the following data: userId,name, surname,
   *     birthDate, email.
   * @return UserDto wrapped in ResponseEntity which contains the following data: userId, name,
   *     surname, birthDate, email, active, paymentCards.
   */
  ResponseEntity<UserDto> createUserInternal(UserCreateRequest request);

  /**
   * Deletes a user while creating a new user in the application from internal sources (AUTH
   * Service) fails. URL: /api/v1/users/internal/{id} with DELETE method
   *
   * @param id the unique identifier (UUID) of the user to delete.
   * @return ResponseEntity with HTTP status 204 (No Content).
   */
  ResponseEntity<Void> deleteUserInternal(UUID id);
}
