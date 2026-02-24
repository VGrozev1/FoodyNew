package com.example.demo.controller;

import com.example.demo.Entities.Order;
import com.example.demo.Entities.OrderItem;
import com.example.demo.Entities.OrderStatus;
import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.dto.OrderItemResponseDto;
import com.example.demo.dto.OrderResponseDto;
import com.example.demo.dto.UpdateOrderStatusRequest;
import jakarta.validation.Valid;
import com.example.demo.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/api/orders")
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(order));
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @GetMapping("/api/orders/{id}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long id) {
        return orderService.findById(id)
                .map(o -> ResponseEntity.ok(toDto(o)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/orders/{id}/items")
    public ResponseEntity<List<OrderItemResponseDto>> getOrderItems(@PathVariable Long id) {
        if (orderService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<OrderItemResponseDto> items = orderService.getOrderItems(id).stream()
                .map(this::itemToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/api/clients/{clientId}/orders")
    public List<OrderResponseDto> getOrdersByClient(@PathVariable Long clientId) {
        return orderService.findByClientId(clientId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/api/orders/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderStatus status;
        try {
            status = OrderStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + request.getStatus());
        }
        return orderService.updateStatus(id, status, request.getDriverId())
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
                o.getStatus().name(),
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

    private OrderItemResponseDto itemToDto(OrderItem item) {
        return new OrderItemResponseDto(
                item.getMenuItem() != null ? item.getMenuItem().getId() : null,
                item.getMenuItem() != null ? item.getMenuItem().getName() : null,
                item.getQuantity(),
                item.getPriceAtOrderTime()
        );
    }
}
