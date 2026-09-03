package com.acme.accountservice.payment;

public record PaymentRecord(
        String name,
        String lastname,
        String period,
        long salary
) {
}
