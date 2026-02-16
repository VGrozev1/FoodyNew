package com.example.demo;

import com.example.demo.Entities.Role;
import com.example.demo.Entities.User;
import com.example.demo.Repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UserTestLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    public UserTestLoader(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("hello@test.com")) {
            User u = new User();
            u.setEmail("hello@test.com");
            u.setPasswordHash("test");
            u.setRole(Role.CLIENT);
            u.setActive(true);

            userRepository.save(u);
            System.out.println("Test user saved");
        }
    }
}
