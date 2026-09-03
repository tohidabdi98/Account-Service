package com.acme.accountservice.auth;

import java.util.List;

public record AccountUser(
        Long id,
        String name,
        String lastname,
        String email,
        String password,
        List<String> roles,
        boolean locked,
        int failedAttempts
) {
    public AccountUser(Long id, String name, String lastname, String email, String password) {
        this(id, name, lastname, email, password, List.of(), false, 0);
    }

    public AccountUser(
            Long id,
            String name,
            String lastname,
            String email,
            String password,
            List<String> roles
    ) {
        this(id, name, lastname, email, password, roles, false, 0);
    }
}
