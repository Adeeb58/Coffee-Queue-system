package com.beanbrewcafe.barista.service;

import com.beanbrewcafe.barista.model.Barista;
import com.beanbrewcafe.barista.model.Order;
import com.beanbrewcafe.barista.repository.BaristaRepository;
import com.beanbrewcafe.barista.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Priority Queue Service
 * Implements the Dynamic Priority Queue with Predictive Scheduling algorithm
 * 
 * This is the CORE of the smart queuing system that:
 * 1. Recalculates priorities every 30 seconds
 * 2. Assigns orders intelligently based on workload
 * 3. Ensures fairness and emergency handling
 * 4. Balances barista workload
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriorityQueueService {

    private final OrderRepository orderRepository;
    private final BaristaRepository baristaRepository;

    private static final int MAX_WAIT_TIME = 10; // minutes
    private static final int EMERGENCY_THRESHOLD = 8; // minutes
    private static final int MAX_SKIP_COUNT = 3;

    /**
     * Recalculate priority scores for all pending orders
     * Called every 30 seconds by scheduler
     */
    @Transactional
    public void recalculatePriorities() {
        List<Order> pendingOrders = orderRepository.findByStatus(Order.OrderStatus.PENDING);

        log.debug("Recalculating priorities for {} pending orders", pendingOrders.size());

        for (Order order : pendingOrders) {
            BigDecimal oldScore = order.getPriorityScore();
            BigDecimal newScore = order.calculatePriorityScore();

            if (!oldScore.equals(newScore)) {
                log.debug("Order {}: Priority {} -> {}",
                        order.getOrderNumber(), oldScore, newScore);
            }

            // Check for emergency flag
            if (order.getCurrentWaitMinutes() >= EMERGENCY_THRESHOLD) {
                order.setEmergencyFlag(true);
                log.warn("Order {} flagged as EMERGENCY (wait time: {} min)",
                        order.getOrderNumber(), order.getCurrentWaitMinutes());
            }
        }

        orderRepository.saveAll(pendingOrders);
    }

    /**
     * Assign next order to an available barista
     * Uses workload balancing and priority scoring
     * 
     * ALGORITHM:
     * 1. Get all pending orders sorted by priority
     * 2. Calculate average barista workload
     * 3. Select best order based on barista's current load
     * 4. Update skip counts for fairness tracking
     * 5. Assign order and update workload
     */
    @Transactional
    public Optional<Order> assignNextOrder(Long baristaId) {
        // Get barista
        Optional<Barista> baristaOpt = baristaRepository.findById(baristaId);
        if (baristaOpt.isEmpty()) {
            log.error("Barista not found: {}", baristaId);
            return Optional.empty();
        }

        Barista barista = baristaOpt.get();

        // Get pending orders sorted by priority
        List<Order> pendingOrders = orderRepository.findPendingOrdersByPriority();

        if (pendingOrders.isEmpty()) {
            log.info("No pending orders for barista {}", barista.getName());
            return Optional.empty();
        }

        // Get average workload for balancing
        Double avgWorkload = baristaRepository.getAverageWorkload();
        if (avgWorkload == null)
            avgWorkload = 0.0;

        // Select best order based on barista's current workload
        Order selectedOrder = selectOrderForBarista(barista, pendingOrders, avgWorkload);

        if (selectedOrder != null) {
            // Assign order
            selectedOrder.setBarista(barista);
            selectedOrder.setStatus(Order.OrderStatus.IN_PROGRESS);
            selectedOrder.setAssignedTime(LocalDateTime.now());

            // Update barista workload
            barista.setCurrentWorkload(
                    barista.getCurrentWorkload() + selectedOrder.getTotalPrepTime());
            barista.setStatus(Barista.BaristaStatus.BUSY);

            // Update skip counts for other orders (fairness tracking)
            updateSkipCounts(selectedOrder, pendingOrders);

            orderRepository.save(selectedOrder);
            baristaRepository.save(barista);

            log.info("Assigned order {} to barista {} (priority: {}, prep time: {} min)",
                    selectedOrder.getOrderNumber(), barista.getName(),
                    selectedOrder.getPriorityScore(), selectedOrder.getTotalPrepTime());

            return Optional.of(selectedOrder);
        }

        return Optional.empty();
    }

    /**
     * Select the best order for a barista based on workload balancing
     * 
     * STRATEGY:
     * - Emergency orders always get highest priority
     * - Overloaded baristas (>1.2x avg) prefer quick orders
     * - Underutilized baristas (<0.8x avg) can take complex orders
     * - Otherwise, assign highest priority order
     */
    private Order selectOrderForBarista(Barista barista, List<Order> orders, Double avgWorkload) {
        double workloadRatio = barista.getCurrentWorkload() / Math.max(avgWorkload, 1.0);

        // RULE 1: Emergency orders get top priority regardless of workload
        for (Order order : orders) {
            if (order.getEmergencyFlag()) {
                return order;
            }
        }

        // RULE 2: If barista is overloaded (>1.2x average), prefer quick orders
        if (workloadRatio > 1.2) {
            log.debug("Barista {} is overloaded ({}x), looking for quick orders",
                    barista.getName(), String.format("%.2f", workloadRatio));

            for (Order order : orders) {
                if (order.getTotalPrepTime() <= 2) { // Quick orders (1-2 min)
                    return order;
                }
            }
        }

        // RULE 3: If barista is underutilized (<0.8x average), can take complex orders
        if (workloadRatio < 0.8) {
            log.debug("Barista {} is underutilized ({}x), can take complex orders",
                    barista.getName(), String.format("%.2f", workloadRatio));
        }

        // RULE 4: Default - return highest priority order
        return orders.get(0);
    }

    /**
     * Update skip counts when an order is served ahead of others
     * This ensures fairness by tracking how many times an order has been "jumped"
     */
    private void updateSkipCounts(Order servedOrder, List<Order> allOrders) {
        for (Order order : allOrders) {
            if (order.getId().equals(servedOrder.getId())) {
                continue;
            }

            // If this order was placed before the served order
            if (order.getOrderTime().isBefore(servedOrder.getOrderTime())) {
                order.setSkippedCount(order.getSkippedCount() + 1);

                if (order.getSkippedCount() > MAX_SKIP_COUNT) {
                    log.warn("Order {} has been skipped {} times",
                            order.getOrderNumber(), order.getSkippedCount());
                }
            }
        }
    }

    /**
     * Complete an order and update barista availability
     */
    @Transactional
    public void completeOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            log.error("Order not found: {}", orderId);
            return;
        }

        Order order = orderOpt.get();
        Barista barista = order.getBarista();

        // Update order
        order.setStatus(Order.OrderStatus.COMPLETED);
        order.setCompletionTime(LocalDateTime.now());
        order.setWaitTimeMinutes(order.getCurrentWaitMinutes());

        // Update barista
        if (barista != null) {
            barista.setCurrentWorkload(
                    Math.max(0, barista.getCurrentWorkload() - order.getTotalPrepTime()));
            barista.setTotalOrdersServed(barista.getTotalOrdersServed() + 1);

            // Check if barista has more work
            List<Order> activeOrders = orderRepository.findActiveOrdersByBarista(barista.getId());
            if (activeOrders.size() <= 1) { // Only this order
                barista.setStatus(Barista.BaristaStatus.AVAILABLE);
            }

            baristaRepository.save(barista);
        }

        orderRepository.save(order);

        log.info("Completed order {} (wait time: {} min)",
                order.getOrderNumber(), order.getWaitTimeMinutes());
    }

    /**
     * Get queue statistics
     */
    public QueueStats getQueueStats() {
        List<Order> pendingOrders = orderRepository.findByStatus(Order.OrderStatus.PENDING);
        Long emergencyCount = orderRepository.countEmergencyOrders();

        int totalPending = pendingOrders.size();
        int avgWaitTime = (int) pendingOrders.stream()
                .mapToInt(Order::getCurrentWaitMinutes)
                .average()
                .orElse(0.0);

        int maxWaitTime = pendingOrders.stream()
                .mapToInt(Order::getCurrentWaitMinutes)
                .max()
                .orElse(0);

        return new QueueStats(totalPending, avgWaitTime, maxWaitTime, emergencyCount.intValue());
    }

    public record QueueStats(int totalPending, int avgWaitTime, int maxWaitTime, int emergencyCount) {
    }
}