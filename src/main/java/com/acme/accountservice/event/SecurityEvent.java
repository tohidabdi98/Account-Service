package com.acme.accountservice.event;

import java.time.Instant;

public record SecurityEvent(
        Long id,
        Instant date,
        String action,
        String subject,
        String object,
        String path
) {
}
