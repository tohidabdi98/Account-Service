package com.acme.accountservice.admin;

import java.util.List;

import com.acme.accountservice.auth.RoleRequest;
import com.acme.accountservice.auth.SignupResponse;
import com.acme.accountservice.auth.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"", "/"})
    public List<SignupResponse> findAll() {
        return userService.findAll().stream()
                .map(SignupResponse::from)
                .toList();
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<AdminStatusResponse> delete(@PathVariable String email) {
        userService.delete(email);
        return ResponseEntity.ok(new AdminStatusResponse(email, "Deleted successfully!"));
    }

    @PutMapping("/role")
    public SignupResponse updateRole(@Valid @RequestBody RoleRequest request) {
        return SignupResponse.from(userService.updateRole(request));
    }
}
