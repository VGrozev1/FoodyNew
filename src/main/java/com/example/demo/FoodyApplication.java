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

    @Bean
    CommandLineRunner ensureRestaurantFilterColumns(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute("alter table if exists restaurants add column if not exists cuisine varchar(64)");
            jdbcTemplate.execute("alter table if exists restaurants add column if not exists price_range integer");
            // Backfill: default cuisine and price, then infer cuisine from name/description
            jdbcTemplate.execute("update restaurants set cuisine = 'general' where cuisine is null or trim(coalesce(cuisine,'')) = ''");
            jdbcTemplate.execute("update restaurants set cuisine = 'burger' where (cuisine = 'general' or cuisine is null) and (lower(coalesce(name,'')) like '%burger%' or lower(coalesce(description,'')) like '%burger%')");
            jdbcTemplate.execute("update restaurants set cuisine = 'pizza' where cuisine = 'general' and (lower(coalesce(name,'')) like '%pizza%' or lower(coalesce(description,'')) like '%pizza%')");
            jdbcTemplate.execute("update restaurants set cuisine = 'sushi' where cuisine = 'general' and (lower(coalesce(name,'')) like '%sushi%' or lower(coalesce(description,'')) like '%sushi%')");
            jdbcTemplate.execute("update restaurants set cuisine = 'asian' where cuisine = 'general' and (lower(coalesce(name,'')) like '%asian%' or lower(coalesce(name,'')) like '%noodle%' or lower(coalesce(description,'')) like '%asian%' or lower(coalesce(description,'')) like '%noodle%')");
            jdbcTemplate.execute("update restaurants set cuisine = 'mexican' where cuisine = 'general' and (lower(coalesce(name,'')) like '%mexican%' or lower(coalesce(name,'')) like '%taco%' or lower(coalesce(description,'')) like '%mexican%' or lower(coalesce(description,'')) like '%taco%')");
            jdbcTemplate.execute("update restaurants set cuisine = 'healthy' where cuisine = 'general' and (lower(coalesce(name,'')) like '%salad%' or lower(coalesce(name,'')) like '%healthy%' or lower(coalesce(description,'')) like '%salad%' or lower(coalesce(description,'')) like '%healthy%')");
            jdbcTemplate.execute("update restaurants set cuisine = 'dessert' where cuisine = 'general' and (lower(coalesce(name,'')) like '%dessert%' or lower(coalesce(name,'')) like '%cake%' or lower(coalesce(name,'')) like '%bakery%' or lower(coalesce(description,'')) like '%dessert%' or lower(coalesce(description,'')) like '%cake%')");
            jdbcTemplate.execute("update restaurants set price_range = 2 where price_range is null");
        };
    }
}
