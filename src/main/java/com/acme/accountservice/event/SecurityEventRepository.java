package com.acme.accountservice.event;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class SecurityEventRepository {

    private final JdbcTemplate jdbcTemplate;

    public SecurityEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(String action, String subject, String object, String path) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO security_events (event_date, action, subject, object, path) VALUES (?, ?, ?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
            );
            statement.setTimestamp(1, Timestamp.from(Instant.now()));
            statement.setString(2, action);
            statement.setString(3, subject);
            statement.setString(4, object);
            statement.setString(5, path);
            return statement;
        }, keyHolder);
    }

    public List<SecurityEvent> findAll() {
        return jdbcTemplate.query(
                """
                SELECT id, event_date, action, subject, object, path
                FROM security_events
                ORDER BY id
                """,
                (resultSet, rowNum) -> new SecurityEvent(
                        resultSet.getLong("id"),
                        resultSet.getTimestamp("event_date").toInstant(),
                        resultSet.getString("action"),
                        resultSet.getString("subject"),
                        resultSet.getString("object"),
                        resultSet.getString("path")
                )
        );
    }
}
