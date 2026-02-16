package com.example.demo.controller;

import com.example.demo.Entities.MenuItem;
import com.example.demo.Entities.Order;
import com.example.demo.Entities.OrderStatus;
import com.example.demo.Entities.Restaurant;
import com.example.demo.dto.*;
import com.example.demo.service.OrderService;
import com.example.demo.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final OrderService orderService;

    public RestaurantController(RestaurantService restaurantService, OrderService orderService) {
        this.restaurantService = restaurantService;
        this.orderService = orderService;
    }

    @GetMapping
    public List<RestaurantDto> getAll() {
        return restaurantService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto> getById(@PathVariable Long id) {
        return restaurantService.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuItemDto>> getMenu(@PathVariable Long id) {
        if (restaurantService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<MenuItemDto> menu = restaurantService.getMenu(id).stream()
                .map(this::menuItemToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(menu);
    }

    @GetMapping("/{id}/dashboard")
    public ResponseEntity<RestaurantDashboardDto> getDashboard(@PathVariable Long id) {
        if (restaurantService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Order> orders = orderService.findByRestaurantId(id);
        LocalDate today = LocalDate.now();
        long totalOrders = orders.size();
        long pendingOrders = orders.stream()
                .filter(o -> o.getStatus() != null && o.getStatus().name() != null
                        && !"DELIVERED".equals(o.getStatus().name()) && !"CANCELLED".equals(o.getStatus().name()))
                .count();
        BigDecimal todayRevenue = orders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().equals(today))
                .map(Order::getTotalPrice)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int menuItemCount = restaurantService.getMenu(id).size();
        return ResponseEntity.ok(new RestaurantDashboardDto(totalOrders, pendingOrders, menuItemCount, todayRevenue));
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderResponseDto>> getOrders(@PathVariable Long id, @RequestParam(required = false) String status) {
        if (restaurantService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            try {
                orders = orderService.findByRestaurantIdAndStatus(id, OrderStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                orders = orderService.findByRestaurantId(id);
            }
        } else {
            orders = orderService.findByRestaurantId(id);
        }
        List<OrderResponseDto> dtos = orders.stream().map(this::orderToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/menuItems")
    public ResponseEntity<MenuItemDto> createMenuItem(@PathVariable Long id, @RequestBody CreateMenuItemRequest request) {
        try {
            MenuItem item = restaurantService.createMenuItem(id, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(menuItemToDto(item));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/menuItems/{itemId}")
    public ResponseEntity<MenuItemDto> updateMenuItem(@PathVariable Long id, @PathVariable Long itemId, @RequestBody CreateMenuItemRequest request) {
        try {
            return restaurantService.updateMenuItem(id, itemId, request)
                    .map(this::menuItemToDto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/menuItems/{itemId}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id, @PathVariable Long itemId) {
        return restaurantService.deleteMenuItem(id, itemId) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private OrderResponseDto orderToDto(Order o) {
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

    private RestaurantDto toDto(Restaurant r) {
        return new RestaurantDto(r.getId(), r.getName(), r.getDescription(), r.getAddress(), r.isOpen());
    }

    private MenuItemDto menuItemToDto(MenuItem m) {
        return new MenuItemDto(m.getId(), m.getName(), m.getDescription(), m.getPrice(), m.isAvailable());
    }
}
