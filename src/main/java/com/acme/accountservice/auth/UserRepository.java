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
                SELECT id, name, lastname, email, password
                FROM users
                WHERE LOWER(email) = LOWER(?)
                """,
                (resultSet, rowNum) -> new AccountUser(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("lastname"),
                        resultSet.getString("email"),
                        resultSet.getString("password")
                ),
                email
        );
    }

    public void updatePassword(String email, String password) {
        jdbcTemplate.update(
                "UPDATE users SET password = ? WHERE LOWER(email) = LOWER(?)",
                password,
                email
        );
    }
}
