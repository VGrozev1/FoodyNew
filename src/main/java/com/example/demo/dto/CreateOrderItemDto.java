package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderItemDto {
    @NotNull(message = "menuItemId is required")
    private Long menuItemId;

    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity;
}
