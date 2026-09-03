package com.acme.accountservice.auth;

import java.util.Set;

final class BreachedPasswords {

    private static final Set<String> VALUES = Set.of(
            "PasswordForJanuary",
            "PasswordForFebruary",
            "PasswordForMarch",
            "PasswordForApril",
            "PasswordForMay",
            "PasswordForJune",
            "PasswordForJuly",
            "PasswordForAugust",
            "PasswordForSeptember",
            "PasswordForOctober",
            "PasswordForNovember",
            "PasswordForDecember"
    );

    private BreachedPasswords() {
    }

    static boolean contains(String password) {
        return VALUES.contains(password);
    }
}
