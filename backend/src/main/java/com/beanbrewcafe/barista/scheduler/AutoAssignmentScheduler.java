package com.beanbrewcafe.barista.scheduler;

import com.beanbrewcafe.barista.model.Barista;
import com.beanbrewcafe.barista.model.Order;
import com.beanbrewcafe.barista.model.Barista.BaristaStatus;
import com.beanbrewcafe.barista.model.Order.OrderStatus;
import com.beanbrewcafe.barista.repository.BaristaRepository;
import com.beanbrewcafe.barista.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AutoAssignmentScheduler {

    private final BaristaRepository baristaRepository;
    private final OrderRepository orderRepository;

    @Scheduled(fixedRate = 30000) // every 30 seconds
    public void assignOrdersAutomatically() {

        List<Barista> availableBaristas = baristaRepository.findByStatus(BaristaStatus.AVAILABLE);

        for (Barista barista : availableBaristas) {

            Optional<Order> orderOpt = orderRepository.findTopByStatusOrderByPriorityScoreDesc(
                    OrderStatus.PENDING);

            if (orderOpt.isEmpty())
                return;

            Order order = orderOpt.get();

            order.setStatus(OrderStatus.IN_PROGRESS);
            order.setBarista(barista);

            barista.setStatus(BaristaStatus.BUSY);
            barista.setCurrentWorkload(
                    barista.getCurrentWorkload() + order.getEstimatedPrepTime());

            orderRepository.save(order);
            baristaRepository.save(barista);
        }
    }
}
