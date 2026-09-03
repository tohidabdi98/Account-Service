package com.acme.accountservice.accountant;

import java.util.List;

import com.acme.accountservice.payment.PaymentRequest;
import com.acme.accountservice.payment.PaymentService;
import com.acme.accountservice.payment.PaymentStatusResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/acct")
public class AccountantController {

    private final PaymentService paymentService;

    public AccountantController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentStatusResponse> addPayments(
            @RequestBody List<@Valid PaymentRequest> requests
    ) {
        paymentService.addPayments(requests);
        return ResponseEntity.ok(new PaymentStatusResponse("Added successfully!"));
    }

    @PutMapping("/payments")
    public ResponseEntity<PaymentStatusResponse> updatePayment(
            @Valid @RequestBody PaymentRequest request
    ) {
        paymentService.updatePayment(request);
        return ResponseEntity.ok(new PaymentStatusResponse("Updated successfully!"));
    }
}
