package com.sims.simscoreservice.salesOrder.repository;

import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Sales Order Repository
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long>,
        JpaSpecificationExecutor<SalesOrder> {

    /**
     * Find latest sales order with pessimistic lock (for generating order reference)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT so FROM SalesOrder so WHERE so.orderReference LIKE CONCAT(:pattern, '%') ORDER BY so.orderReference DESC LIMIT 1")
    Optional<SalesOrder> findLatestSalesOrderWithPessimisticLock(@Param("pattern") String pattern);

    /**
     * Count outgoing (pending) sales orders
     */
    @Query("SELECT COUNT(so) FROM SalesOrder so WHERE so.status IN ('PARTIALLY_APPROVED', 'PENDING', 'PARTIALLY_DELIVERED')")
    Long countOutgoingSalesOrders();

    /**
     * Find all outgoing sales orders
     */
    @Query("SELECT so FROM SalesOrder so WHERE so.status IN ('PARTIALLY_APPROVED', 'PENDING', 'PARTIALLY_DELIVERED')")
    Page<SalesOrder> findAllOutgoingSalesOrders(Pageable pageable);

    /**
     * Find all urgent sales orders (delivery date < 2 days)
     */
    @Query("SELECT so FROM SalesOrder so WHERE so.status IN ('PARTIALLY_APPROVED', 'PENDING', 'PARTIALLY_DELIVERED') " +
            "AND so.estimatedDeliveryDate < :twoDaysFromNow")
    Page<SalesOrder> findAllUrgentSalesOrders(Pageable pageable, @Param("twoDaysFromNow") LocalDateTime twoDaysFromNow);

    /**
     * Search in pending sales orders (IC context)
     */
    @Query("""
        SELECT DISTINCT so FROM SalesOrder so
        JOIN so.items i
        JOIN i.product p
        WHERE so.status IN ('PARTIALLY_APPROVED', 'PENDING', 'PARTIALLY_DELIVERED')
        AND (
            LOWER(so.customerName) LIKE LOWER(CONCAT('%', :text, '%'))
            OR LOWER(so.orderReference) LIKE LOWER(CONCAT('%', :text, '%'))
        )
    """)
    Page<SalesOrder> searchInWaitingSalesOrders(@Param("text") String text, Pageable pageable);

    /**
     * Search in all sales orders (OM context)
     */
    @Query("""
        SELECT DISTINCT so FROM SalesOrder so
        JOIN so.items i
        JOIN i.product p
        WHERE LOWER(so.customerName) LIKE LOWER(CONCAT('%', :text, '%'))
        OR LOWER(so.orderReference) LIKE LOWER(CONCAT('%', :text, '%'))
    """)
    Page<SalesOrder> searchInSalesOrders(@Param("text") String text, Pageable pageable);

    /**
     * Count active orders for a product
     */
    @Query("""
        SELECT COUNT(DISTINCT so.id)
        FROM SalesOrder so
        JOIN so.items oi
        WHERE oi.product.productId = :productId
        AND so.status IN ('PENDING', 'PARTIALLY_APPROVED', 'PARTIALLY_DELIVERED', 
                         'APPROVED', 'DELIVERY_IN_PROCESS')
    """)
    long countActiveOrdersForProduct(@Param("productId") String productId);

    /**
     * Get all active orders for a product
     */
    @Query("""
        SELECT DISTINCT so
        FROM SalesOrder so
        JOIN so.items oi
        WHERE oi.product.productId = :productId
        AND so.status IN ('PENDING', 'PARTIALLY_APPROVED', 'PARTIALLY_DELIVERED',
                         'APPROVED', 'DELIVERY_IN_PROCESS')
    """)
    List<SalesOrder> findActiveOrdersForProduct(@Param("productId") String productId);

    /**
     * Count in-progress sales orders
     */
    @Query("SELECT COUNT(so) FROM SalesOrder so WHERE so.status NOT IN ('DELIVERED', 'CANCELLED')")
    Long countInProgressSalesOrders();

    /**
     * Count completed sales orders in date range
     */
    @Query("""
        SELECT COUNT(so)
        FROM SalesOrder so
        WHERE so.orderDate BETWEEN :startDate AND :endDate
        AND so.status IN ('DELIVERED', 'COMPLETED')
    """)
    Long countCompletedSalesOrdersBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
}