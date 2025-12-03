package com.sims.simscoreservice.salesOrder.repository;

import com.sims.simscoreservice.salesOrder.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order Item Repository
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Calculate total revenue for completed orders in date range
     */
    @Query("""
        SELECT COALESCE(SUM(oi.quantity * oi.product.price), 0)
        FROM OrderItem oi
        JOIN oi.salesOrder so
        WHERE so.orderDate BETWEEN :startDate AND :endDate
        AND so.status IN ('DELIVERED', 'COMPLETED')
    """)
    BigDecimal calculateTotalRevenue(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);
}
