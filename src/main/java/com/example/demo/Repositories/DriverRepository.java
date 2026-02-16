package com.example.demo.Repositories;

import com.example.demo.Entities.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByUserId(Long userId);

    List<Driver> findByAvailableTrue();

    Optional<Driver> findFirstByAvailableTrue();
}
