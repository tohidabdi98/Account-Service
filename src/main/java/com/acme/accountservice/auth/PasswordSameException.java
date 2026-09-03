package com.acme.accountservice.auth;

public class PasswordSameException extends RuntimeException {

    public PasswordSameException() {
        super("The passwords must be different!");
    }
}
