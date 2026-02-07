package com.beanbrewcafe.barista.scheduler;

import com.beanbrewcafe.barista.model.Order;
import com.beanbrewcafe.barista.model.Order.OrderStatus;
import com.beanbrewcafe.barista.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderPriorityScheduler {

    private final OrderRepository orderRepository;

    @Scheduled(fixedRate = 60000) // every 1 minute
    public void updateOrderPriority() {

        List<Order> orders = orderRepository.findByStatus(OrderStatus.PENDING);

        for (Order order : orders) {

            int waitMinutes = (int) Duration.between(
                    order.getOrderTime(),
                    LocalDateTime.now()).toMinutes();

            order.setCurrentWaitMinutes(waitMinutes);

            double priority = order.getEstimatedPrepTime() * 5;

            priority += waitMinutes * 2;

            if (waitMinutes >= 8 && !order.isEmergencyFlag()) {
                order.setEmergencyFlag(true);
                priority += 50;
            }

            order.setPriorityScore(java.math.BigDecimal.valueOf(priority));
        }

        orderRepository.saveAll(orders);
    }
}
