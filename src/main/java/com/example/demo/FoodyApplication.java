package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
}
