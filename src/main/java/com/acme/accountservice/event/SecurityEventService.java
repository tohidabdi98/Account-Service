package com.acme.accountservice.event;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class SecurityEventService {

    private final SecurityEventRepository eventRepository;

    public SecurityEventService(SecurityEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void log(String action, String subject, String object, String path) {
        eventRepository.save(action, subject, object, path);
    }

    public List<SecurityEvent> findAll() {
        return eventRepository.findAll();
    }
}
