package com.acme.accountservice.employee;

import com.acme.accountservice.auth.AccountUser;
import com.acme.accountservice.auth.UserService;
import com.acme.accountservice.payment.PaymentResponse;
import com.acme.accountservice.payment.PaymentService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empl")
public class EmployeeController {

    private final UserService userService;
    private final PaymentService paymentService;

    public EmployeeController(UserService userService, PaymentService paymentService) {
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @GetMapping({"/payment", "/payment/"})
    public ResponseEntity<?> payment(
            Authentication authentication,
            @RequestParam(required = false) String period
    ) {
        AccountUser user = userService.findByEmail(authentication.getName());
        List<PaymentResponse> payments = paymentService.findPayments(user.email(), period);
        if (period == null || period.isBlank()) {
            return ResponseEntity.ok(payments);
        }
        return payments.isEmpty()
                ? ResponseEntity.ok(java.util.Map.of())
                : ResponseEntity.ok(payments.get(0));
    }
}
