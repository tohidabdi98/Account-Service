package com.acme.accountservice.auth;

import java.sql.PreparedStatement;
import java.sql.Statement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsByEmailIgnoreCase(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)",
                Integer.class,
                email
        );
        return count != null && count > 0;
    }

    public int countUsers() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        return count == null ? 0 : count;
    }

    public AccountUser save(String name, String lastname, String email, String password) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users (name, lastname, email, password) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, name);
            statement.setString(2, lastname);
            statement.setString(3, email);
            statement.setString(4, password);
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException("The database did not return a generated user id");
        }
        return new AccountUser(generatedId.longValue(), name, lastname, email, password);
    }

    public AccountUser findByEmailIgnoreCase(String email) {
        return jdbcTemplate.queryForObject(
                """
                SELECT id, name, lastname, email, password, locked, failed_attempts
                FROM users
                WHERE LOWER(email) = LOWER(?)
                """,
                (resultSet, rowNum) -> mapUser(resultSet),
                email
        );
    }

    public AccountUser findById(long id) {
        return jdbcTemplate.queryForObject(
                "SELECT id, name, lastname, email, password, locked, failed_attempts FROM users WHERE id = ?",
                (resultSet, rowNum) -> mapUser(resultSet),
                id
        );
    }

    public java.util.List<AccountUser> findAll() {
        return jdbcTemplate.query(
                "SELECT id, name, lastname, email, password, locked, failed_attempts FROM users ORDER BY id",
                (resultSet, rowNum) -> mapUser(resultSet)
        );
    }

    public java.util.List<String> findRoles(long userId) {
        return jdbcTemplate.queryForList(
                        "SELECT role FROM user_roles WHERE user_id = ?",
                        String.class,
                        userId
                ).stream()
                .sorted()
                .toList();
    }

    public void addRole(long userId, String role) {
        jdbcTemplate.update(
                "INSERT INTO user_roles (user_id, role) VALUES (?, ?)",
                userId,
                role
        );
    }

    public void removeRole(long userId, String role) {
        jdbcTemplate.update(
                "DELETE FROM user_roles WHERE user_id = ? AND role = ?",
                userId,
                role
        );
    }

    public void deletePayments(String email) {
        jdbcTemplate.update("DELETE FROM payments WHERE LOWER(employee) = LOWER(?)", email);
    }

    public void deleteUser(long userId) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);
    }

    public void updatePassword(String email, String password) {
        jdbcTemplate.update(
                "UPDATE users SET password = ? WHERE LOWER(email) = LOWER(?)",
                password,
                email
        );
    }

    public int incrementFailedAttempts(String email) {
        jdbcTemplate.update(
                "UPDATE users SET failed_attempts = failed_attempts + 1 WHERE LOWER(email) = LOWER(?)",
                email
        );
        Integer attempts = jdbcTemplate.queryForObject(
                "SELECT failed_attempts FROM users WHERE LOWER(email) = LOWER(?)",
                Integer.class,
                email
        );
        return attempts == null ? 0 : attempts;
    }

    public void resetFailedAttempts(String email) {
        jdbcTemplate.update(
                "UPDATE users SET failed_attempts = 0 WHERE LOWER(email) = LOWER(?)",
                email
        );
    }

    public void lockUser(String email) {
        jdbcTemplate.update(
                "UPDATE users SET locked = TRUE WHERE LOWER(email) = LOWER(?)",
                email
        );
    }

    public void unlockUser(String email) {
        jdbcTemplate.update(
                "UPDATE users SET locked = FALSE, failed_attempts = 0 WHERE LOWER(email) = LOWER(?)",
                email
        );
    }

    private AccountUser mapUser(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        long id = resultSet.getLong("id");
        return new AccountUser(
                id,
                resultSet.getString("name"),
                resultSet.getString("lastname"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                findRoles(id),
                resultSet.getBoolean("locked"),
                resultSet.getInt("failed_attempts")
        );
    }
}
