package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    private Long id;
    private String status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private Long clientId;
    private Long restaurantId;
    private Long driverId;
    private LocalDateTime estimatedDeliveryAt;
    private String driverName;
    private String driverPhone;
    private String restaurantName;
    private String deliveryAddress;
}
