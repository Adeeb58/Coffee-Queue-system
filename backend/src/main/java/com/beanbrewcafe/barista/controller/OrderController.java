package com.beanbrewcafe.barista.controller;

import com.beanbrewcafe.barista.dto.OrderRequest;
import com.beanbrewcafe.barista.dto.OrderResponse;
import com.beanbrewcafe.barista.model.Order;
import com.beanbrewcafe.barista.service.OrderService;
import com.beanbrewcafe.barista.service.PriorityQueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;
    private final PriorityQueueService priorityQueueService;

    /**
     * Create a new order
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        Order order = orderService.createOrder(
                request.getDrinkId(),
                request.getQuantity(),
                request.getCustomerPhone(),
                request.getCustomerName());
        return ResponseEntity.ok(OrderResponse.fromOrder(order));
    }

    /**
     * Get all orders
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get pending orders sorted by priority
     * GET /api/orders/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<OrderResponse>> getPendingOrders() {
        List<Order> orders = orderService.getPendingOrders();
        List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get order by ID
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(OrderResponse::fromOrder)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Complete an order
     * POST /api/orders/{id}/complete
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeOrder(@PathVariable Long id) {
        priorityQueueService.completeOrder(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Cancel an order
     * DELETE /api/orders/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Get queue statistics
     * GET /api/orders/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<PriorityQueueService.QueueStats> getQueueStats() {
        return ResponseEntity.ok(priorityQueueService.getQueueStats());
    }
}