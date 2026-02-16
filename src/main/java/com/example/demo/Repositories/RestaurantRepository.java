package com.example.demo.Repositories;

import com.example.demo.Entities.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByUserId(Long userId);

    Optional<Restaurant> findByUserEmail(String email);

    List<Restaurant> findByOpenTrue();

    List<Restaurant> findByNameContainingIgnoreCase(String name);
}
