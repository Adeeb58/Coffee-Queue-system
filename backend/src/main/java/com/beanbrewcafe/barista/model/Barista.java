package com.beanbrewcafe.barista.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "baristas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Barista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BaristaStatus status = BaristaStatus.AVAILABLE;

    @Column(name = "current_workload")
    private Integer currentWorkload = 0; // in minutes

    @Column(name = "total_orders_served")
    private Integer totalOrdersServed = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum BaristaStatus {
        AVAILABLE, BUSY, OFFLINE
    }
}