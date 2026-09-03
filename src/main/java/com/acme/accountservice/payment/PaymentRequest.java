package com.acme.accountservice.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record PaymentRequest(
        @NotBlank String employee,
        @NotBlank
        @Pattern(regexp = "^(0[1-9]|1[0-2])-\\d{4}$", message = "Wrong date!")
        String period,
        @NotNull
        @PositiveOrZero(message = "Salary must be non negative!")
        Long salary
) {
}
