package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class FoodyApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodyApplication.class, args);
    }

    @Bean
    CommandLineRunner testDatabaseConnection(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                System.out.println("✅ Connected to Supabase!");
                System.out.println("DB URL: " + connection.getMetaData().getURL());
                System.out.println("DB User: " + connection.getMetaData().getUserName());
            }
        };
    }

    @Bean
    CommandLineRunner ensureVerificationColumns(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute("alter table if exists users add column if not exists email_verified boolean");
            jdbcTemplate.execute("alter table if exists users add column if not exists verification_code varchar(255)");
            jdbcTemplate.execute("alter table if exists users add column if not exists verification_code_expires_at timestamp");

            // Existing accounts are considered verified to avoid locking old users.
            jdbcTemplate.execute("update users set email_verified = true where email_verified is null");
        };
    }
}
