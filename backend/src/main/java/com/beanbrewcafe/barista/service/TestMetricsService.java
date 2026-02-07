package com.beanbrewcafe.barista.service;

import com.beanbrewcafe.barista.model.Barista;
import com.beanbrewcafe.barista.model.Order;
import com.beanbrewcafe.barista.repository.BaristaRepository;
import com.beanbrewcafe.barista.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestMetricsService {

    private final OrderRepository orderRepository;
    private final BaristaRepository baristaRepository;

    /**
     * Get comprehensive test metrics
     */
    public TestMetrics getTestMetrics() {
        // Filter only test orders
        List<Order> allOrders = orderRepository.findByIsTestOrder(true);
        List<Barista> baristas = baristaRepository.findAll();

        TestMetrics metrics = new TestMetrics();

        // Overall metrics
        metrics.setTotalOrders(allOrders.size());
        metrics.setPendingOrders(countByStatus(allOrders, Order.OrderStatus.PENDING));
        metrics.setInProgressOrders(countByStatus(allOrders, Order.OrderStatus.IN_PROGRESS));
        metrics.setCompletedOrders(countByStatus(allOrders, Order.OrderStatus.COMPLETED));

        // Wait time metrics
        // Wait time metrics (Calculated for ALL orders to show real-time status)
        if (!allOrders.isEmpty()) {
            double avgWait = allOrders.stream()
                    .mapToInt(Order::getCurrentWaitMinutes)
                    .average()
                    .orElse(0.0);

            int maxWait = allOrders.stream()
                    .mapToInt(Order::getCurrentWaitMinutes)
                    .max()
                    .orElse(0);

            int minWait = allOrders.stream()
                    .mapToInt(Order::getCurrentWaitMinutes)
                    .min()
                    .orElse(0);

            metrics.setAvgWaitTime(Double.parseDouble(String.format("%.1f", avgWait)));
            metrics.setMaxWaitTime(maxWait);
            metrics.setMinWaitTime(minWait);

            // Timeout rate (orders waiting > 10 min)
            long timeouts = allOrders.stream()
                    .filter(o -> o.getCurrentWaitMinutes() > 10)
                    .count();

            double rate = (double) timeouts / allOrders.size() * 100;
            metrics.setTimeoutRate(Double.parseDouble(String.format("%.1f", rate)));
        } else {
            metrics.setAvgWaitTime(0.0);
            metrics.setMaxWaitTime(0);
            metrics.setMinWaitTime(0);
            metrics.setTimeoutRate(0.0);
        }

        // Barista-specific metrics
        Map<String, BaristaMetrics> baristaMetricsMap = new HashMap<>();

        for (Barista barista : baristas) {
            BaristaMetrics bm = new BaristaMetrics();
            bm.setBaristaName(barista.getName());
            bm.setCurrentWorkload(barista.getCurrentWorkload());
            bm.setTotalOrdersServed(barista.getTotalOrdersServed());

            // Get orders by this barista
            List<Order> baristaOrders = allOrders.stream()
                    .filter(o -> o.getBarista() != null && o.getBarista().getId().equals(barista.getId()))
                    .collect(Collectors.toList());

            // Orders by drink type
            Map<String, Long> ordersByDrink = baristaOrders.stream()
                    .collect(Collectors.groupingBy(
                            o -> o.getDrink().getName(),
                            Collectors.counting()));
            bm.setOrdersByDrinkType(ordersByDrink);

            // Order status breakdown
            bm.setPendingCount(countByBaristaAndStatus(baristaOrders, Order.OrderStatus.PENDING));
            bm.setInProgressCount(countByBaristaAndStatus(baristaOrders, Order.OrderStatus.IN_PROGRESS));
            bm.setCompletedCount(countByBaristaAndStatus(baristaOrders, Order.OrderStatus.COMPLETED));

            baristaMetricsMap.put(barista.getName(), bm);
        }

        metrics.setBaristaMetrics(baristaMetricsMap);

        // Drink distribution
        Map<String, Long> drinkDistribution = allOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getDrink().getName(),
                        Collectors.counting()));
        metrics.setDrinkDistribution(drinkDistribution);

        return metrics;
    }

    /**
     * Get time-series data for simulation
     */
    public List<TimeSeriesPoint> getTimeSeriesData() {
        List<Order> allOrders = orderRepository.findAll();

        // Group orders by minute
        Map<LocalDateTime, List<Order>> ordersByMinute = allOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getOrderTime().withSecond(0).withNano(0)));

        List<TimeSeriesPoint> timeSeries = new ArrayList<>();

        for (Map.Entry<LocalDateTime, List<Order>> entry : ordersByMinute.entrySet()) {
            TimeSeriesPoint point = new TimeSeriesPoint();
            point.setTimestamp(entry.getKey());
            point.setOrderCount(entry.getValue().size());

            long pending = entry.getValue().stream()
                    .filter(o -> o.getStatus() == Order.OrderStatus.PENDING)
                    .count();
            point.setPendingCount((int) pending);

            long inProgress = entry.getValue().stream()
                    .filter(o -> o.getStatus() == Order.OrderStatus.IN_PROGRESS)
                    .count();
            point.setInProgressCount((int) inProgress);

            long completed = entry.getValue().stream()
                    .filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED)
                    .count();
            point.setCompletedCount((int) completed);

            timeSeries.add(point);
        }

        // Sort by timestamp
        timeSeries.sort(Comparator.comparing(TimeSeriesPoint::getTimestamp));

        return timeSeries;
    }

    private int countByStatus(List<Order> orders, Order.OrderStatus status) {
        return (int) orders.stream()
                .filter(o -> o.getStatus() == status)
                .count();
    }

    private int countByBaristaAndStatus(List<Order> orders, Order.OrderStatus status) {
        return (int) orders.stream()
                .filter(o -> o.getStatus() == status)
                .count();
    }

    // DTOs
    public static class TestMetrics {
        private Integer totalOrders;
        private Integer pendingOrders;
        private Integer inProgressOrders;
        private Integer completedOrders;
        private Double avgWaitTime;
        private Integer maxWaitTime;
        private Integer minWaitTime;
        private Double timeoutRate;
        private Map<String, BaristaMetrics> baristaMetrics;
        private Map<String, Long> drinkDistribution;

        // Getters and setters
        public Integer getTotalOrders() {
            return totalOrders;
        }

        public void setTotalOrders(Integer totalOrders) {
            this.totalOrders = totalOrders;
        }

        public Integer getPendingOrders() {
            return pendingOrders;
        }

        public void setPendingOrders(Integer pendingOrders) {
            this.pendingOrders = pendingOrders;
        }

        public Integer getInProgressOrders() {
            return inProgressOrders;
        }

        public void setInProgressOrders(Integer inProgressOrders) {
            this.inProgressOrders = inProgressOrders;
        }

        public Integer getCompletedOrders() {
            return completedOrders;
        }

        public void setCompletedOrders(Integer completedOrders) {
            this.completedOrders = completedOrders;
        }

        public Double getAvgWaitTime() {
            return avgWaitTime;
        }

        public void setAvgWaitTime(Double avgWaitTime) {
            this.avgWaitTime = avgWaitTime;
        }

        public Integer getMaxWaitTime() {
            return maxWaitTime;
        }

        public void setMaxWaitTime(Integer maxWaitTime) {
            this.maxWaitTime = maxWaitTime;
        }

        public Integer getMinWaitTime() {
            return minWaitTime;
        }

        public void setMinWaitTime(Integer minWaitTime) {
            this.minWaitTime = minWaitTime;
        }

        public Double getTimeoutRate() {
            return timeoutRate;
        }

        public void setTimeoutRate(Double timeoutRate) {
            this.timeoutRate = timeoutRate;
        }

        public Map<String, BaristaMetrics> getBaristaMetrics() {
            return baristaMetrics;
        }

        public void setBaristaMetrics(Map<String, BaristaMetrics> baristaMetrics) {
            this.baristaMetrics = baristaMetrics;
        }

        public Map<String, Long> getDrinkDistribution() {
            return drinkDistribution;
        }

        public void setDrinkDistribution(Map<String, Long> drinkDistribution) {
            this.drinkDistribution = drinkDistribution;
        }
    }

    public static class BaristaMetrics {
        private String baristaName;
        private Integer currentWorkload;
        private Integer totalOrdersServed;
        private Integer pendingCount;
        private Integer inProgressCount;
        private Integer completedCount;
        private Map<String, Long> ordersByDrinkType;

        // Getters and setters
        public String getBaristaName() {
            return baristaName;
        }

        public void setBaristaName(String baristaName) {
            this.baristaName = baristaName;
        }

        public Integer getCurrentWorkload() {
            return currentWorkload;
        }

        public void setCurrentWorkload(Integer currentWorkload) {
            this.currentWorkload = currentWorkload;
        }

        public Integer getTotalOrdersServed() {
            return totalOrdersServed;
        }

        public void setTotalOrdersServed(Integer totalOrdersServed) {
            this.totalOrdersServed = totalOrdersServed;
        }

        public Integer getPendingCount() {
            return pendingCount;
        }

        public void setPendingCount(Integer pendingCount) {
            this.pendingCount = pendingCount;
        }

        public Integer getInProgressCount() {
            return inProgressCount;
        }

        public void setInProgressCount(Integer inProgressCount) {
            this.inProgressCount = inProgressCount;
        }

        public Integer getCompletedCount() {
            return completedCount;
        }

        public void setCompletedCount(Integer completedCount) {
            this.completedCount = completedCount;
        }

        public Map<String, Long> getOrdersByDrinkType() {
            return ordersByDrinkType;
        }

        public void setOrdersByDrinkType(Map<String, Long> ordersByDrinkType) {
            this.ordersByDrinkType = ordersByDrinkType;
        }
    }

    public static class TimeSeriesPoint {
        private LocalDateTime timestamp;
        private Integer orderCount;
        private Integer pendingCount;
        private Integer inProgressCount;
        private Integer completedCount;

        // Getters and setters
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public Integer getOrderCount() {
            return orderCount;
        }

        public void setOrderCount(Integer orderCount) {
            this.orderCount = orderCount;
        }

        public Integer getPendingCount() {
            return pendingCount;
        }

        public void setPendingCount(Integer pendingCount) {
            this.pendingCount = pendingCount;
        }

        public Integer getInProgressCount() {
            return inProgressCount;
        }

        public void setInProgressCount(Integer inProgressCount) {
            this.inProgressCount = inProgressCount;
        }

        public Integer getCompletedCount() {
            return completedCount;
        }

        public void setCompletedCount(Integer completedCount) {
            this.completedCount = completedCount;
        }
    }
}