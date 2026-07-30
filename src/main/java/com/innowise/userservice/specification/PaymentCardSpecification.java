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

  public static Specification<PaymentCard> hasFirstName(String name) {
    return ((root, query, criteriaBuilder) ->
        name == null ? null : criteriaBuilder.equal(root.get("user").get("name"), name));
  }

  public static Specification<PaymentCard> hasLastName(String surname) {
    return ((root, query, criteriaBuilder) ->
        surname == null ? null : criteriaBuilder.equal(root.get("user").get("surname"), surname));
  }
}
