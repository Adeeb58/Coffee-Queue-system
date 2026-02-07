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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkloadScheduler {

    private final BaristaRepository baristaRepository;
    private final OrderRepository orderRepository;

    @Scheduled(fixedRate = 60000) // every 1 minute
    public void reduceBaristaWorkload() {

        List<Barista> baristas = baristaRepository.findAll();

        for (Barista barista : baristas) {

            if (barista.getCurrentWorkload() > 0) {
                barista.setCurrentWorkload(barista.getCurrentWorkload() - 1);
            }

            if (barista.getCurrentWorkload() <= 0) {

                barista.setCurrentWorkload(0);
                barista.setStatus(BaristaStatus.AVAILABLE);

                // 🔑 COMPLETE the active order
                Optional<Order> activeOrder = orderRepository.findByBaristaAndStatus(
                        barista,
                        OrderStatus.IN_PROGRESS);

                activeOrder.ifPresent(order -> {
                    order.setStatus(OrderStatus.COMPLETED);
                    order.setCompletionTime(LocalDateTime.now());
                    orderRepository.save(order);

                    // 🔑 THIS is why count was 0
                    barista.setTotalOrdersServed(
                            barista.getTotalOrdersServed() + 1);
                });
            }
        }

        baristaRepository.saveAll(baristas);
    }
}
