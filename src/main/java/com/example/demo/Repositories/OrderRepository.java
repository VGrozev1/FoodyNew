package com.example.demo.Repositories;

import com.example.demo.Entities.Order;
import com.example.demo.Entities.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByClientId(Long clientId);

    List<Order> findByClientIdAndStatus(Long clientId, OrderStatus status);

    List<Order> findByRestaurantId(Long restaurantId);

    List<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status);

    List<Order> findByDriverId(Long driverId);

    List<Order> findByDriverIdAndStatusIn(Long driverId, List<OrderStatus> statuses);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
