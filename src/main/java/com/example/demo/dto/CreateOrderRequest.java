package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull(message = "clientId is required")
    private Long clientId;

    @NotNull(message = "restaurantId is required")
    private Long restaurantId;

    @NotBlank(message = "deliveryAddress is required")
    @Size(max = 255, message = "deliveryAddress is too long")
    private String deliveryAddress;

    @NotEmpty(message = "items are required")
    @Valid
    private List<CreateOrderItemDto> items;
}
