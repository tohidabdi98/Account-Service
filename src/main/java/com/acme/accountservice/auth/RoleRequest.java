package com.acme.accountservice.auth;

import jakarta.validation.constraints.NotBlank;

public record RoleRequest(
        @NotBlank String user,
        @NotBlank String role,
        @NotBlank String operation
) {
}
