package com.acme.accountservice.admin;

import com.acme.accountservice.auth.AccessRequest;
import com.acme.accountservice.auth.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user")
public class AccessController {

    private final UserService userService;

    public AccessController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/access")
    public ResponseEntity<AccessStatusResponse> updateAccess(
            Authentication authentication,
            @Valid @RequestBody AccessRequest request
    ) {
        String status = userService.updateAccess(request, authentication.getName());
        return ResponseEntity.ok(new AccessStatusResponse(status));
    }
}
