package com.beanbrewcafe.barista.dto;

import com.beanbrewcafe.barista.model.Order;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private String drinkName;
    private Integer quantity;
    private String status;
    private BigDecimal priorityScore;
    private Integer currentWaitMinutes;
    private Boolean emergencyFlag;
    private String baristaName;
    private LocalDateTime orderTime;
    private Integer estimatedPrepTime;

    public static OrderResponse fromOrder(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setDrinkName(order.getDrink().getName());
        response.setQuantity(order.getQuantity());
        response.setStatus(order.getStatus().name());
        response.setPriorityScore(order.getPriorityScore());
        response.setCurrentWaitMinutes(order.getCurrentWaitMinutes());
        response.setEmergencyFlag(order.isEmergencyFlag());
        response.setOrderTime(order.getOrderTime());
        response.setEstimatedPrepTime(order.getEstimatedPrepTime());

        if (order.getBarista() != null) {
            response.setBaristaName(order.getBarista().getName());
        }

        return response;
    }
}