package com.acme.accountservice.auth;

public record ChangePasswordResponse(String email, String status) {

    public static ChangePasswordResponse success(String email) {
        return new ChangePasswordResponse(email, "The password has been updated successfully");
    }
}
