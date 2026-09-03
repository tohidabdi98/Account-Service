package com.acme.accountservice.security;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.acme.accountservice.event.SecurityEventActions;
import com.acme.accountservice.event.SecurityEventService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final SecurityEventService eventService;

    public RestAccessDeniedHandler(ObjectMapper objectMapper, SecurityEventService eventService) {
        this.objectMapper = objectMapper;
        this.eventService = eventService;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String subject = authentication == null || authentication.getName() == null
                ? "Anonymous"
                : authentication.getName();
        eventService.log(SecurityEventActions.ACCESS_DENIED, subject, request.getRequestURI(), request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", 403);
        body.put("error", "Forbidden");
        body.put("message", "Access Denied!");
        body.put("path", request.getRequestURI());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
