package com.acme.accountservice.payment;

public record PaymentResponse(
        String name,
        String lastname,
        String period,
        String salary
) {
}
