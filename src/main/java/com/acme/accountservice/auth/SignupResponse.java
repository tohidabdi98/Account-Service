package com.acme.accountservice.auth;

public record SignupResponse(
        Long id,
        String name,
        String lastname,
        String email,
        java.util.List<String> roles
) {
    public static SignupResponse from(AccountUser user) {
        return new SignupResponse(user.id(), user.name(), user.lastname(), user.email(), user.roles());
    }
}
