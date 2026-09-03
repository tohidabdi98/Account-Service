package com.acme.accountservice.security;

import com.acme.accountservice.auth.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventListener {

    private final UserService userService;

    public AuthenticationEventListener(UserService userService) {
        this.userService = userService;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        userService.handleSuccessfulLogin(event.getAuthentication().getName());
    }
}
