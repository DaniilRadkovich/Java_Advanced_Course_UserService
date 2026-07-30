package com.innowise.userservice.specification;

import com.innowise.userservice.model.entity.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSpecification {
  public static Specification<User> hasName(String name) {
    return ((root, query, criteriaBuilder) ->
        name == null ? null : criteriaBuilder.equal(root.get("name"), name));
  }

  public static Specification<User> hasSurname(String surname) {
    return ((root, query, criteriaBuilder) ->
        surname == null ? null : criteriaBuilder.equal(root.get("surname"), surname));
  }
}
