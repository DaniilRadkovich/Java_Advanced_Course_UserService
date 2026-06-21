package com.innowise.userservice.service;

import com.innowise.userservice.exception.EntityValidationException;
import com.innowise.userservice.model.dto.UserDto;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface defining the business contract for managing users. Provides methods for CRUD
 * operations, account status editing, and card aggregation.
 */
public interface UserService {

  /**
   * Creates a new user in the application.
   *
   * <p>Validates if the email is unique before persisting. Sets the initial status to active. On
   * successful persistence, the created user data is stored in the cache.
   *
   * @param userDto containing new user details (userId, name, surname, birthDate, email, active,
   *     paymentCards).
   * @return the created UserDto enriched with its generated identifier.
   */
  UserDto createUser(UserDto userDto);

  /**
   * Finds a specific user by their ID.
   *
   * @param id the unique identifier (UUID) of the user.
   * @return the UserDto (userId, name, surname, birthDate, email, active, paymentCards) matching
   *     the given ID.
   */
  UserDto getUserById(UUID id);

  /**
   * Finds a filtered and paginated list of all users.
   *
   * <p>Filters users dynamically using specifications based on the provided name and surname.
   *
   * @param name optional filter for the user's first name.
   * @param surname optional filter for the user's surname.
   * @param pageable pagination and sorting configuration.
   * @return a Page of UserDto (userId, name, surname, birthDate, email, active, paymentCards)
   *     matching the entered data.
   */
  Page<UserDto> getAllUsers(String name, String surname, Pageable pageable);

  /**
   * Updates an existing user's information.
   *
   * <p>Updates changed fields using the mapper, flushes changes to the database, and updates the
   * cache with the new user state.
   *
   * @param userId the unique identifier (UUID) of the user to update.
   * @param userDto the UserDto (userId, name, surname, birthDate, email, active, paymentCards)
   *     containing new values.
   * @return the updated UserDto.
   */
  UserDto updateUserById(UUID userId, UserDto userDto);

  /**
   * Activates a user account by their ID.
   *
   * <p>Sets the user's active status to true and evicts their outdated record from the cache.
   *
   * @param userId the unique identifier (UUID) of the user to activate.
   */
  void activateUser(UUID userId);

  /**
   * Deactivates a user account by their ID.
   *
   * <p>Sets the user's active status to false and evicts their outdated record from the cache.
   *
   * @param userId the unique identifier (UUID) of the user to deactivate.
   */
  void deactivateUser(UUID userId);

  /**
   * Counts the total number of payment cards linked to a specific user.
   *
   * @param userId the unique identifier (UUID) of the user.
   * @return the total number of associated payment cards.
   */
  int getCardCount(UUID userId);

  /**
   * Permanently deletes a user from the system by their ID.
   *
   * <p>Removes the user record from the database and completely removes their entries from the
   * cache.
   *
   * @param userId the unique identifier (UUID) of the user to delete.
   */
  void deleteUserById(UUID userId);
}
