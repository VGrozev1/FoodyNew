package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private Long clientId;
    private Long restaurantId;
    private String deliveryAddress;
    private List<CreateOrderItemDto> items;
}
