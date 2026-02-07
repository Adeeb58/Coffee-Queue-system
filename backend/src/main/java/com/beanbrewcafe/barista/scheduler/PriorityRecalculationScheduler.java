package com.beanbrewcafe.barista.scheduler;

import com.beanbrewcafe.barista.model.Order;
import com.beanbrewcafe.barista.model.Order.OrderStatus;
import com.beanbrewcafe.barista.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PriorityRecalculationScheduler {

    private final OrderRepository orderRepository;

    @Scheduled(fixedRate = 30000) // every 30 seconds
    public void recalculatePriorities() {

        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);

        for (Order order : pendingOrders) {
            order.calculatePriorityScore(); // 🔥 dynamic recalculation
        }

        orderRepository.saveAll(pendingOrders);
    }
}
