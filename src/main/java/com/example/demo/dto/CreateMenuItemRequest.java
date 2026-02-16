package com.example.demo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateMenuItemRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private boolean available = true;
}
