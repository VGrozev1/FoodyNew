package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateMenuItemRequest {
    @NotBlank(message = "name is required")
    @Size(max = 120, message = "name is too long")
    private String name;

    @Size(max = 500, message = "description is too long")
    private String description;

    @NotNull(message = "price is required")
    @DecimalMin(value = "0.01", message = "price must be greater than 0")
    private BigDecimal price;

    private boolean available = true;
}
