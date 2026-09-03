package com.acme.accountservice.auth;

public class PasswordPolicyException extends RuntimeException {

    public PasswordPolicyException(String message) {
        super(message);
    }
}
