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

    List<Restaurant> findByCuisineIgnoreCase(String cuisine);

    List<Restaurant> findByPriceRange(Integer priceRange);

    List<Restaurant> findByCuisineIgnoreCaseAndPriceRange(String cuisine, Integer priceRange);

    List<Restaurant> findByPriceRangeIn(java.util.Collection<Integer> priceRanges);

    List<Restaurant> findByCuisineIgnoreCaseAndPriceRangeIn(String cuisine, java.util.Collection<Integer> priceRanges);
}
