package com.beanbrewcafe.barista.service;

import com.beanbrewcafe.barista.model.Drink;
import com.beanbrewcafe.barista.model.Order;
import com.beanbrewcafe.barista.repository.BaristaRepository;
import com.beanbrewcafe.barista.repository.DrinkRepository;
import com.beanbrewcafe.barista.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestDataService {

    private final OrderService orderService;
    private final DrinkRepository drinkRepository;
    private final OrderRepository orderRepository;
    private final BaristaRepository baristaRepository;

    /**
     * Generate test orders following Poisson distribution
     * λ = 1.4 customers/minute (as per requirements)
     */
    @Transactional
    public List<Order> generate100TestOrders() {
        log.info("Starting test order generation - 100 orders");

        List<Order> createdOrders = new ArrayList<>();
        List<Drink> drinks = drinkRepository.findAll();
        List<com.beanbrewcafe.barista.model.Barista> baristas = baristaRepository.findAll();

        if (drinks.isEmpty()) {
            throw new RuntimeException("No drinks available. Please run schema.sql first.");
        }

        if (baristas.isEmpty()) {
            throw new RuntimeException("No baristas available. Please ensure baristas are created.");
        }

        // Calculate drink selection based on frequency
        Map<Long, Double> drinkProbabilities = calculateDrinkProbabilities(drinks);

        // Generate orders with Poisson distribution
        // Start 2 hours ago to ensure meaningful wait times immediately
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(60);
        double lambda = 1.4; // customers per minute

        for (int i = 0; i < 100; i++) {
            // Calculate arrival time using exponential distribution
            double intervalMinutes = -Math.log(1 - Math.random()) / lambda;
            LocalDateTime orderTime = startTime.plusMinutes((long) (i * intervalMinutes));

            // Select drink based on frequency distribution
            Long drinkId = selectDrinkByProbability(drinkProbabilities);

            // Random quantity (1-3, with 70% being 1)
            int quantity = Math.random() < 0.7 ? 1 : (Math.random() < 0.8 ? 2 : 3);

            // Generate customer info (30% have loyalty)
            String customerName = "TestCustomer" + (i + 1);
            String customerPhone = String.format("98765%05d", i);

            // Create order with specific time
            Order order = createOrderWithTime(drinkId, quantity, customerPhone, customerName, orderTime);

            // Mark as test order
            order.setIsTestOrder(true);

            // Auto-assign to barista using round-robin
            com.beanbrewcafe.barista.model.Barista assignedBarista = baristas.get(i % baristas.size());
            order.setBarista(assignedBarista);
            order.setStatus(Order.OrderStatus.IN_PROGRESS);
            order.setAssignedTime(LocalDateTime.now());

            // Update barista workload
            assignedBarista.setCurrentWorkload(
                    assignedBarista.getCurrentWorkload() + order.getDrink().getPrepTime());
            baristaRepository.save(assignedBarista);

            order = orderRepository.save(order);
            createdOrders.add(order);

            if ((i + 1) % 10 == 0) {
                log.info("Generated {} orders", i + 1);
            }
        }

        log.info("Successfully generated 100 test orders and auto-assigned to baristas");
        return createdOrders;
    }

    /**
     * Calculate drink selection probabilities based on frequency
     */
    private Map<Long, Double> calculateDrinkProbabilities(List<Drink> drinks) {
        Map<Long, Double> probabilities = new HashMap<>();
        double cumulativeProbability = 0.0;

        for (Drink drink : drinks) {
            cumulativeProbability += drink.getFrequency().doubleValue() / 100.0;
            probabilities.put(drink.getId(), cumulativeProbability);
        }

        return probabilities;
    }

    /**
     * Select drink based on probability distribution
     */
    private Long selectDrinkByProbability(Map<Long, Double> probabilities) {
        double random = Math.random();

        for (Map.Entry<Long, Double> entry : probabilities.entrySet()) {
            if (random <= entry.getValue()) {
                return entry.getKey();
            }
        }

        // Fallback to first drink
        return probabilities.keySet().iterator().next();
    }

    /**
     * Create order with specific timestamp
     */
    private Order createOrderWithTime(Long drinkId, Integer quantity,
            String phone, String name, LocalDateTime orderTime) {
        Order order = orderService.createOrder(drinkId, quantity, phone, name);

        // Update order time to match test scenario
        order.setOrderTime(orderTime);
        order.calculatePriorityScore();

        return orderRepository.save(order);
    }

    /**
     * Clear all test data
     */
    @Transactional
    public void clearTestData() {
        log.info("Clearing test data");
        List<Order> testOrders = orderRepository.findByIsTestOrder(true);
        orderRepository.deleteAll(testOrders);
        log.info("Cleared {} test orders", testOrders.size());
    }
}