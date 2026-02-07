package com.beanbrewcafe.barista.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false, length = 20)
    private String orderNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "drink_id", nullable = false)
    private Drink drink;

    @ManyToOne
    @JoinColumn(name = "barista_id")
    private Barista barista;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "priority_score", precision = 5, scale = 2)
    private BigDecimal priorityScore = BigDecimal.ZERO;

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;

    @Column(name = "assigned_time")
    private LocalDateTime assignedTime;

    @Column(name = "completion_time")
    private LocalDateTime completionTime;

    @Column(name = "wait_time_minutes")
    private Integer waitTimeMinutes;

    @Column(name = "skipped_count")
    private Integer skippedCount = 0;

    @Column(name = "emergency_flag")
    private boolean emergencyFlag = false;

    @Column(name = "is_test_order")
    private Boolean isTestOrder = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (orderTime == null) {
            orderTime = LocalDateTime.now();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * CORE ALGORITHM: Calculate priority score based on:
     * - Wait time (40%): Longer wait = higher priority
     * - Order complexity (25%): Shorter orders get bonus
     * - Loyalty status (10%): Gold members get boost
     * - Urgency (25%): Approaching timeout gets significant boost
     * 
     * @return BigDecimal priority score (0-100)
     */
    public BigDecimal calculatePriorityScore() {
        double score = 0.0;

        // 1. Wait Time Component (40% weight)
        long waitMinutes = Math.max(
                ChronoUnit.MINUTES.between(orderTime, LocalDateTime.now()),
                0);
        double waitScore = Math.min(waitMinutes * 4.0, 40.0); // Max 40 points
        score += waitScore;

        // 2. Order Complexity Component (25% weight)
        // Shorter prep times get higher scores for better throughput
        int prepTime = drink.getPrepTime() * quantity;
        double complexityScore = 25.0 - (prepTime * 2.5); // 1 min = 22.5, 6 min = 10
        complexityScore = Math.max(complexityScore, 10.0); // Minimum 10 points
        score += complexityScore;

        // 3. Loyalty Status Component (10% weight)
        if (customer != null) {
            switch (customer.getLoyaltyStatus()) {
                case GOLD -> score += 10.0;
                case REGULAR -> score += 5.0;
                case NEW -> score += 0.0;
            }
        }

        // 4. Urgency Component (25% weight)
        // Emergency boost for orders approaching timeout
        if (waitMinutes >= 8) {
            // CRITICAL: Customer approaching 10-minute hard limit
            score += 50.0; // Significant emergency boost
            this.emergencyFlag = true;
        } else if (waitMinutes >= 6) {
            score += 25.0;
        } else if (waitMinutes >= 4) {
            score += 15.0;
        }

        // 5. Fairness Penalty
        // If too many people have been skipped, increase priority
        if (skippedCount > 3) {
            score += skippedCount * 5.0;
        }

        // Cap at 100 points
        this.priorityScore = BigDecimal.valueOf(Math.min(score, 100.0));
        return this.priorityScore;
    }

    /**
     * Get current wait time in minutes
     */
    public void setCurrentWaitMinutes(Integer minutes) {
        this.waitTimeMinutes = minutes;
    }

    public Integer getCurrentWaitMinutes() {
        if (waitTimeMinutes != null) {
            return Math.max(waitTimeMinutes, 0);
        }

        if (orderTime == null) {
            return 0;
        }

        long minutes = ChronoUnit.MINUTES.between(orderTime, LocalDateTime.now());
        return (int) Math.max(minutes, 0);
    }

    /**
     * Get total preparation time for this order
     */
    public Integer getEstimatedPrepTime() {
        return drink.getPrepTime() * quantity;
    }

    public enum OrderStatus {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }
}