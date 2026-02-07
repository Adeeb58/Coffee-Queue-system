package com.beanbrewcafe.barista.service;

import com.beanbrewcafe.barista.model.Customer;
import com.beanbrewcafe.barista.model.Drink;
import com.beanbrewcafe.barista.model.Order;

import com.beanbrewcafe.barista.repository.CustomerRepository;
import com.beanbrewcafe.barista.repository.DrinkRepository;
import com.beanbrewcafe.barista.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final DrinkRepository drinkRepository;
    private final CustomerRepository customerRepository;

    /**
     * Create a new order
     */
    @Transactional
    public Order createOrder(Long drinkId, Integer quantity, String customerPhone, String customerName) {
        // Get or create customer
        Customer customer = null;
        if (customerPhone != null && !customerPhone.isEmpty()) {
            customer = customerRepository.findByPhone(customerPhone)
                    .orElseGet(() -> {
                        Customer newCustomer = new Customer();
                        newCustomer.setPhone(customerPhone);
                        newCustomer.setName(customerName);
                        newCustomer.setLoyaltyStatus(Customer.LoyaltyStatus.NEW);
                        return customerRepository.save(newCustomer);
                    });

            // Update visit count
            customer.setTotalVisits(customer.getTotalVisits() + 1);

            // Update loyalty status based on visits
            if (customer.getTotalVisits() >= 50) {
                customer.setLoyaltyStatus(Customer.LoyaltyStatus.GOLD);
            } else if (customer.getTotalVisits() >= 10) {
                customer.setLoyaltyStatus(Customer.LoyaltyStatus.REGULAR);
            }

            customerRepository.save(customer);
        }

        // Get drink
        Drink drink = drinkRepository.findById(drinkId)
                .orElseThrow(() -> new RuntimeException("Drink not found"));

        // Create order
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setDrink(drink);
        order.setCustomer(customer);
        order.setQuantity(quantity);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setOrderTime(LocalDateTime.now());

        // Calculate initial priority
        order.calculatePriorityScore();

        Order savedOrder = orderRepository.save(order);

        log.info("Created order: {} - {} x{} (Priority: {})",
                savedOrder.getOrderNumber(),
                drink.getName(),
                quantity,
                savedOrder.getPriorityScore());

        return savedOrder;
    }

    /**
     * Get all pending orders sorted by priority
     */
    public List<Order> getPendingOrders() {
        return orderRepository.findPendingOrdersByPriority();
    }

    /**
     * Get order by ID
     */
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * Get all orders
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Cancel an order
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("Cancelled order: {}", order.getOrderNumber());
    }

    /**
     * Get today's orders
     */
    public List<Order> getTodaysOrders() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(7).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(22).withMinute(0).withSecond(0);
        return orderRepository.findOrdersByTimeRange(startOfDay, endOfDay);
    }

    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }

}