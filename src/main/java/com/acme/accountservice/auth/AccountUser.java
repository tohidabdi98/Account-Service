package com.acme.accountservice.auth;

public record AccountUser(
        Long id,
        String name,
        String lastname,
        String email,
        String password
) {
}
