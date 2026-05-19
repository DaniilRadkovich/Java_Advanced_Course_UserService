package com.innowise.userservice.specification;

import com.innowise.userservice.model.entity.PaymentCard;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentCardSpecification {
  public static Specification<PaymentCard> hasHolder(String holder) {
    return ((root, query, criteriaBuilder) ->
        holder == null ? null : criteriaBuilder.equal(root.get("holder"), holder));
  }
}
