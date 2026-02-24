package com.example.demo.controller;

import com.example.demo.Entities.Order;
import com.example.demo.Entities.OrderStatus;
import com.example.demo.dto.OrderResponseDto;
import com.example.demo.service.DriverService;
import com.example.demo.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;
    private final OrderService orderService;

    public DriverController(DriverService driverService, OrderService orderService) {
        this.driverService = driverService;
        this.orderService = orderService;
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderResponseDto>> getOrders(@PathVariable Long id, @RequestParam(required = false) String status) {
        if (driverService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Order> orders;
        if ("ACTIVE".equalsIgnoreCase(status)) {
            orders = orderService.findActiveByDriverId(id);
            if (orders.isEmpty()) {
                orders = orderService.findActiveOrders();
            }
        } else {
            orders = orderService.findActiveByDriverId(id);
        }

        return ResponseEntity.ok(orders.stream().map(this::toDto).collect(Collectors.toList()));
    }

    private OrderResponseDto toDto(Order o) {
        java.time.LocalDateTime eta = o.getCreatedAt() != null ? o.getCreatedAt().plusMinutes(30) : null;
        String driverName = null;
        String driverPhone = null;
        if (o.getDriver() != null && o.getDriver().getUser() != null) {
            driverName = o.getDriver().getUser().getEmail();
        }
        String restaurantName = o.getRestaurant() != null ? o.getRestaurant().getName() : null;
        return new OrderResponseDto(
                o.getId(),
                o.getStatus() != null ? o.getStatus().name() : OrderStatus.CREATED.name(),
                o.getTotalPrice(),
                o.getCreatedAt(),
                o.getClient() != null ? o.getClient().getId() : null,
                o.getRestaurant() != null ? o.getRestaurant().getId() : null,
                o.getDriver() != null ? o.getDriver().getId() : null,
                eta,
                driverName,
                driverPhone,
                restaurantName,
                o.getDeliveryAddress()
        );
    }
}
