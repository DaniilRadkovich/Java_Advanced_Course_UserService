package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.PaymentCard;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, UUID> {

  @Query(value = "SELECT * FROM payment_cards WHERE number = :number", nativeQuery = true)
  Optional<PaymentCard> findByNumber(String number);

  Page<PaymentCard> findAll(Specification<PaymentCard> specification, Pageable pageable);

  @Query(value = "SELECT * FROM payment_cards WHERE user_id = :userId", nativeQuery = true)
  List<PaymentCard> findPaymentCardByUserId(UUID userId);
}
