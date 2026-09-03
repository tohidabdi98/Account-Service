package com.acme.accountservice.security;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.acme.accountservice.auth.UserService;
import com.acme.accountservice.event.SecurityEventActions;
import com.acme.accountservice.event.SecurityEventService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final SecurityEventService eventService;

    public RestAuthenticationEntryPoint(
            ObjectMapper objectMapper,
            UserService userService,
            SecurityEventService eventService
    ) {
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.eventService = eventService;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        String username = basicUsername(request);
        if (username != null) {
            userService.handleFailedLogin(username, request.getRequestURI());
        } else {
            eventService.log(
                    SecurityEventActions.ACCESS_DENIED,
                    "Anonymous",
                    request.getRequestURI(),
                    request.getRequestURI()
            );
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", Instant.now().toString());
        error.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        error.put("error", "Unauthorized");
        error.put("message", "");
        error.put("path", request.getRequestURI());
        objectMapper.writeValue(response.getWriter(), error);
    }

    private String basicUsername(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Basic ", 0, 6)) {
            return null;
        }
        try {
            String encoded = header.substring(6).trim();
            String credentials = new String(
                    java.util.Base64.getDecoder().decode(encoded),
                    java.nio.charset.StandardCharsets.UTF_8
            );
            int separator = credentials.indexOf(':');
            return separator < 0 ? credentials : credentials.substring(0, separator);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
