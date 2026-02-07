package com.beanbrewcafe.barista.util;

import com.beanbrewcafe.barista.model.Order;
import com.beanbrewcafe.barista.model.Order.OrderStatus;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

public class SimulationUtils {

    private static final Random random = new Random();

    public static Order randomOrder() {
        Order order = new Order();

        order.setOrderNumber("SIM-" + UUID.randomUUID().toString().substring(0, 6));
        order.setQuantity(random.nextInt(3) + 1);
        order.setOrderTime(LocalDateTime.now().minusMinutes(random.nextInt(10)));
        order.setStatus(OrderStatus.PENDING);
        order.setEmergencyFlag(random.nextBoolean());

        return order;
    }
}
