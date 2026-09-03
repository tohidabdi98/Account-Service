package com.acme.accountservice.auth;

import java.util.Locale;
import java.util.Set;

public final class RoleNames {

    public static final String ADMINISTRATOR = "ROLE_ADMINISTRATOR";
    public static final String USER = "ROLE_USER";
    public static final String ACCOUNTANT = "ROLE_ACCOUNTANT";
    public static final String AUDITOR = "ROLE_AUDITOR";

    public static final Set<String> ALL = Set.of(ADMINISTRATOR, USER, ACCOUNTANT, AUDITOR);
    public static final Set<String> ADMINISTRATIVE = Set.of(ADMINISTRATOR);
    public static final Set<String> BUSINESS = Set.of(USER, ACCOUNTANT, AUDITOR);

    private RoleNames() {
    }

    public static String normalize(String role) {
        if (role == null || role.isBlank()) {
            return "";
        }
        String value = role.toUpperCase(Locale.ROOT);
        return value.startsWith("ROLE_") ? value : "ROLE_" + value;
    }
}
