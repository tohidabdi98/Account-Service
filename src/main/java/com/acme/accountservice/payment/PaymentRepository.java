package com.acme.accountservice.payment;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsByEmployeeAndPeriod(String employee, String period) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payments WHERE LOWER(employee) = LOWER(?) AND period = ?",
                Integer.class,
                employee,
                period
        );
        return count != null && count > 0;
    }

    public void save(String employee, String period, long salary) {
        jdbcTemplate.update(
                "INSERT INTO payments (employee, period, salary) VALUES (?, ?, ?)",
                employee,
                period,
                salary
        );
    }

    public void updateSalary(String employee, String period, long salary) {
        int updated = jdbcTemplate.update(
                "UPDATE payments SET salary = ? WHERE LOWER(employee) = LOWER(?) AND period = ?",
                salary,
                employee,
                period
        );
        if (updated == 0) {
            throw new PaymentException("Payment not found!");
        }
    }

    public List<PaymentRecord> findByEmployee(String employee) {
        return jdbcTemplate.query(
                """
                SELECT u.name, u.lastname, p.period, p.salary
                FROM payments p
                JOIN users u ON LOWER(u.email) = LOWER(p.employee)
                WHERE LOWER(p.employee) = LOWER(?)
                """,
                (resultSet, rowNum) -> new PaymentRecord(
                        resultSet.getString("name"),
                        resultSet.getString("lastname"),
                        resultSet.getString("period"),
                        resultSet.getLong("salary")
                ),
                employee
        );
    }

    public PaymentRecord findByEmployeeAndPeriod(String employee, String period) {
        List<PaymentRecord> records = jdbcTemplate.query(
                """
                SELECT u.name, u.lastname, p.period, p.salary
                FROM payments p
                JOIN users u ON LOWER(u.email) = LOWER(p.employee)
                WHERE LOWER(p.employee) = LOWER(?) AND p.period = ?
                """,
                (resultSet, rowNum) -> new PaymentRecord(
                        resultSet.getString("name"),
                        resultSet.getString("lastname"),
                        resultSet.getString("period"),
                        resultSet.getLong("salary")
                ),
                employee,
                period
        );
        return records.isEmpty() ? null : records.get(0);
    }
}
