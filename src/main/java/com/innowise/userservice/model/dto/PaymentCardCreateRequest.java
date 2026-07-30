package com.innowise.userservice.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCardCreateRequest {

  private UUID id;

  @NotBlank(message = "Card number should not be empty!")
  @Pattern(regexp = "\\d{16}", message = "Card number must be 16 chars!")
  private String number;

  @NotBlank(message = "Card holder should not be empty!")
  @Size(min = 1, max = 100, message = "Card holder must be from 1 to 100 chars!")
  private String holder;

  @NotNull(message = "Expiration date should not be empty!")
  @Future(message = "Expiration date must be in the future!")
  private LocalDate expirationDate;
}
