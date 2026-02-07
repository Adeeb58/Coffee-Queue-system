package com.beanbrewcafe.barista.repository;

import com.beanbrewcafe.barista.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(Order.OrderStatus status);

    List<Order> findByStatusOrderByPriorityScoreDesc(Order.OrderStatus status);

    Optional<Order> findTopByStatusOrderByPriorityScoreDesc(Order.OrderStatus status);

    Optional<Order> findByBarista_NameAndStatus(String baristaName, Order.OrderStatus status);

    Optional<Order> findByBaristaAndStatus(com.beanbrewcafe.barista.model.Barista barista, Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' ORDER BY o.priorityScore DESC, o.orderTime ASC")
    List<Order> findPendingOrdersByPriority();

    @Query("SELECT o FROM Order o WHERE o.barista.id = :baristaId AND o.status = 'IN_PROGRESS'")
    List<Order> findActiveOrdersByBarista(Long baristaId);

    @Query("SELECT o FROM Order o WHERE o.orderTime BETWEEN :startTime AND :endTime")
    List<Order> findOrdersByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PENDING' AND o.emergencyFlag = true")
    Long countEmergencyOrders();

    @Query("SELECT AVG(o.waitTimeMinutes) FROM Order o WHERE o.status = 'COMPLETED' AND o.orderTime >= :startTime")
    Double getAverageWaitTime(LocalDateTime startTime);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'COMPLETED' AND o.waitTimeMinutes > 10 AND o.orderTime >= :startTime")
    Long countTimeoutOrders(LocalDateTime startTime);

    List<Order> findByBaristaIdAndStatus(Long baristaId, Order.OrderStatus status);

    // Test order filtering
    List<Order> findByIsTestOrder(Boolean isTestOrder);

    List<Order> findByStatusAndIsTestOrder(Order.OrderStatus status, Boolean isTestOrder);

    List<Order> findByBaristaIdAndStatusAndIsTestOrder(Long baristaId, Order.OrderStatus status, Boolean isTestOrder);

    Optional<Order> findTopByBaristaAndStatus(com.beanbrewcafe.barista.model.Barista barista, Order.OrderStatus status);
}