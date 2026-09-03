package com.acme.accountservice.payment;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.acme.accountservice.auth.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserService userService;

    public PaymentService(PaymentRepository paymentRepository, UserService userService) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
    }

    @Transactional
    public void addPayments(List<PaymentRequest> requests) {
        if (requests.isEmpty()) {
            throw new PaymentException("Payments must not be empty!");
        }

        for (PaymentRequest request : requests) {
            String period = normalizePeriod(request.period());
            ensureEmployeeExists(request.employee());
            if (paymentRepository.existsByEmployeeAndPeriod(request.employee(), period)) {
                throw new PaymentException("Payment for this employee and period already exists!");
            }
        }

        for (PaymentRequest request : requests) {
            try {
                paymentRepository.save(request.employee(), normalizePeriod(request.period()), request.salary());
            } catch (DuplicateKeyException exception) {
                throw new PaymentException("Payment for this employee and period already exists!");
            }
        }
    }

    @Transactional
    public void updatePayment(PaymentRequest request) {
        String period = normalizePeriod(request.period());
        ensureEmployeeExists(request.employee());
        paymentRepository.updateSalary(request.employee(), period, request.salary());
    }

    public List<PaymentResponse> findPayments(String employee, String requestedPeriod) {
        if (requestedPeriod == null || requestedPeriod.isBlank()) {
            return paymentRepository.findByEmployee(employee).stream()
                    .sorted(Comparator.comparing(
                            (PaymentRecord record) -> parsePeriod(record.period()),
                            Comparator.reverseOrder()
                    ))
                    .map(this::toResponse)
                    .toList();
        }

        String period = normalizePeriod(requestedPeriod);
        PaymentRecord record = paymentRepository.findByEmployeeAndPeriod(employee, period);
        return record == null ? List.of() : List.of(toResponse(record));
    }

    private void ensureEmployeeExists(String employee) {
        try {
            userService.findByEmail(employee);
        } catch (RuntimeException exception) {
            throw new PaymentException("Employee not found!");
        }
    }

    private PaymentResponse toResponse(PaymentRecord record) {
        YearMonth period = parsePeriod(record.period());
        long dollars = record.salary() / 100;
        long cents = record.salary() % 100;
        String displayPeriod = period.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + "-" + period.getYear();
        return new PaymentResponse(
                record.name(),
                record.lastname(),
                displayPeriod,
                dollars + " dollar(s) " + cents + " cent(s)"
        );
    }

    private String normalizePeriod(String period) {
        return formatPeriod(parsePeriod(period));
    }

    private YearMonth parsePeriod(String period) {
        if (period == null || !period.matches("^(0[1-9]|1[0-2])-\\d{4}$")) {
            throw new PeriodFormatException("Wrong date!");
        }
        return YearMonth.of(
                Integer.parseInt(period.substring(3)),
                Integer.parseInt(period.substring(0, 2))
        );
    }

    private String formatPeriod(YearMonth period) {
        return String.format("%02d-%04d", period.getMonthValue(), period.getYear());
    }
}
