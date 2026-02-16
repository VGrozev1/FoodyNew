package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDashboardDto {
    private long totalOrders;
    private long pendingOrders;
    private long menuItemCount;
    private BigDecimal todayRevenue;
}
