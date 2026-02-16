package com.example.demo.service;

import com.example.demo.Entities.Driver;
import com.example.demo.Repositories.DriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public Optional<Driver> findById(Long id) {
        return driverRepository.findById(id);
    }

    public Optional<Driver> findByUserId(Long userId) {
        return driverRepository.findByUserId(userId);
    }

    public List<Driver> findAvailable() {
        return driverRepository.findByAvailableTrue();
    }

    public Optional<Driver> findFirstAvailable() {
        return driverRepository.findFirstByAvailableTrue();
    }
}
