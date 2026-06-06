package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  @Query("SELECT u FROM User u WHERE u.email = :email")
  Optional<User> findByEmail(String email);

  Page<User> findAll(Specification<User> specification, Pageable pageable);

  @Query("SELECT COUNT(c) FROM PaymentCard c WHERE c.user.id = :userId")
  int getCardCount(UUID userId);
}
