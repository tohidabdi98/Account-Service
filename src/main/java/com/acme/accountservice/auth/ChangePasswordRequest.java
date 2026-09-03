package com.acme.accountservice.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @JsonProperty("new_password")
        @NotBlank
        String newPassword
) {
}
