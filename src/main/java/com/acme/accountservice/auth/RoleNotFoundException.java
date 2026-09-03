package com.acme.accountservice.auth;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException() {
        super("Role not found!");
    }
}
