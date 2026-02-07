package com.beanbrewcafe.barista.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardStats {
    private Integer totalOrders;
    private Integer pendingOrders;
    private Integer inProgressOrders;
    private Integer completedOrders;
    private Integer avgWaitTime;
    private Integer maxWaitTime;
    private Integer emergencyOrders;
    private Double timeoutRate;
}