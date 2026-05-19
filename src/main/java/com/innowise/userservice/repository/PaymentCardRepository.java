package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.PaymentCard;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, UUID> {

  Optional<PaymentCard> findByNumber(String number);

  Page<PaymentCard> findAll(Specification<PaymentCard> specification, Pageable pageable);

  List<PaymentCard> findPaymentCardByUserId(UUID userId);
}
