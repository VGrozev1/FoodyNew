package com.example.demo.dto;

import lombok.Data;

@Data
public class CreateOrderItemDto {
    private Long menuItemId;
    private int quantity;
}
