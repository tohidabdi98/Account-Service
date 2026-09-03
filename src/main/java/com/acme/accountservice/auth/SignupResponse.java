package com.acme.accountservice.auth;

public record SignupResponse(
        String name,
        String lastname,
        String email
) {
    public static SignupResponse from(SignupRequest request) {
        return new SignupResponse(request.name(), request.lastname(), request.email());
    }
}
