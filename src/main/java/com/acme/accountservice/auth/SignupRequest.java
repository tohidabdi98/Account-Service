package com.acme.accountservice.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(
        @NotBlank String name,
        @NotBlank String lastname,
        @NotBlank
        @Email
        @Pattern(regexp = "^[^@\\s]+@acme\\.com$", message = "Email must be a corporate @acme.com address")
        String email,
        @NotBlank String password
) {
}
