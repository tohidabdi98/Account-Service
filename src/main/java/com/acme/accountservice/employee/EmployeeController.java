package com.acme.accountservice.employee;

import com.acme.accountservice.auth.AccountUser;
import com.acme.accountservice.auth.SignupResponse;
import com.acme.accountservice.auth.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empl")
public class EmployeeController {

    private final UserService userService;

    public EmployeeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/payment", "/payment/"})
    public ResponseEntity<SignupResponse> payment(Authentication authentication) {
        AccountUser user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(SignupResponse.from(user));
    }
}
