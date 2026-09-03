package com.acme.accountservice.auth;

import jakarta.validation.constraints.NotBlank;

public record AccessRequest(
        @NotBlank String user,
        @NotBlank String operation
) {
}
