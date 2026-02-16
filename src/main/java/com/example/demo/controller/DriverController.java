package com.example.demo.controller;

import com.example.demo.Entities.Order;
import com.example.demo.dto.OrderResponseDto;
import com.example.demo.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final OrderService orderService;

    public DriverController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}/orders")
    public List<OrderResponseDto> getOrders(@PathVariable Long id, @RequestParam(required = false) String status) {
        List<Order> orders = "ACTIVE".equalsIgnoreCase(status)
                ? orderService.findActiveByDriverId(id)
                : orderService.findByDriverId(id);
        return orders.stream().map(this::toDto).collect(Collectors.toList());
    }

    private OrderResponseDto toDto(Order o) {
        java.time.LocalDateTime eta = o.getCreatedAt() != null ? o.getCreatedAt().plusMinutes(30) : null;
        String driverName = (o.getDriver() != null && o.getDriver().getUser() != null) ? o.getDriver().getUser().getEmail() : null;
        String restaurantName = o.getRestaurant() != null ? o.getRestaurant().getName() : null;
        return new OrderResponseDto(
                o.getId(), o.getStatus().name(), o.getTotalPrice(), o.getCreatedAt(),
                o.getClient() != null ? o.getClient().getId() : null,
                o.getRestaurant() != null ? o.getRestaurant().getId() : null,
                o.getDriver() != null ? o.getDriver().getId() : null,
                eta, driverName, null, restaurantName,
                o.getDeliveryAddress() != null ? o.getDeliveryAddress() : ""
        );
    }
}
