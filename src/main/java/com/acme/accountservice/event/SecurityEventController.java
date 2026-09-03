package com.acme.accountservice.event;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security")
public class SecurityEventController {

    private final SecurityEventService eventService;

    public SecurityEventController(SecurityEventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping({"/events", "/events/"})
    public List<SecurityEvent> findAll() {
        return eventService.findAll();
    }
}
