package com.acme.accountservice.auth;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleDataLoader implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public RoleDataLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        jdbcTemplate.update("MERGE INTO roles (name) KEY (name) VALUES (?)", RoleNames.ADMINISTRATOR);
        jdbcTemplate.update("MERGE INTO roles (name) KEY (name) VALUES (?)", RoleNames.USER);
        jdbcTemplate.update("MERGE INTO roles (name) KEY (name) VALUES (?)", RoleNames.ACCOUNTANT);
        jdbcTemplate.update("MERGE INTO roles (name) KEY (name) VALUES (?)", RoleNames.AUDITOR);
        jdbcTemplate.update(
                """
                INSERT INTO user_roles (user_id, role)
                SELECT u.id,
                       CASE WHEN u.id = (SELECT MIN(id) FROM users)
                            THEN 'ROLE_ADMINISTRATOR'
                            ELSE 'ROLE_USER'
                       END
                FROM users u
                WHERE NOT EXISTS (
                    SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id
                )
                """
        );
    }
}
