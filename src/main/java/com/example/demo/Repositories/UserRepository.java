package com.example.demo.Repositories;

import com.example.demo.Entities.Role;
import com.example.demo.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndPasswordHash(String email, String passwordHash);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);
}
