package com.sims.simscoreservice.purchaseOrder.repository;


import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Purchase Order Repository
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {

    /**
     * Check if PO number exists
     */
    boolean existsByPoNumber(String poNumber);

    /**
     * Search all orders (OM context)
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE " +
            "LOWER(po.product.name) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(po.supplier.name) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(po.poNumber) LIKE LOWER(CONCAT('%', :text, '%'))")
    Page<PurchaseOrder> searchOrders(@Param("text") String text, Pageable pageable);

    /**
     * Search pending orders only (IC context)
     */
    @Query("""
            SELECT po FROM PurchaseOrder po
            WHERE po.status IN ('DELIVERY_IN_PROCESS', 'PARTIALLY_RECEIVED', 'AWAITING_APPROVAL')
            AND (
                LOWER(po.product.name) LIKE LOWER(CONCAT('%', :text, '%')) OR
                LOWER(po.supplier.name) LIKE LOWER(CONCAT('%', :text, '%')) OR
                LOWER(po.orderedBy) LIKE LOWER(CONCAT('%', :text, '%')) OR
                LOWER(po.updatedBy) LIKE LOWER(CONCAT('%', :text, '%')) OR
                LOWER(po.poNumber) LIKE LOWER(CONCAT('%', :text, '%'))
            )
            """)
    Page<PurchaseOrder> searchInPendingOrders(@Param("text") String text, Pageable pageable);

    /**
     * Count pending purchase orders
     */
    @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE " +
            "po.status IN ('DELIVERY_IN_PROCESS', 'PARTIALLY_RECEIVED', 'AWAITING_APPROVAL')")
    Long countIncomingPurchaseOrders();

    /**
     * Find all pending orders
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE " +
            "po.status IN ('DELIVERY_IN_PROCESS', 'PARTIALLY_RECEIVED', 'AWAITING_APPROVAL')")
    Page<PurchaseOrder> findAllPendingOrders(Pageable pageable);

    /**
     * Find all overdue orders
     */
    @Query("SELECT po FROM PurchaseOrder po " +
            "WHERE po.status IN ('DELIVERY_IN_PROCESS', 'PARTIALLY_RECEIVED', 'AWAITING_APPROVAL') " +
            "AND po.expectedArrivalDate < CURRENT_DATE")
    Page<PurchaseOrder> findAllOverdueOrders(Pageable pageable);

    /**
     * Find by product ID
     */
    List<PurchaseOrder> findByProduct_ProductId(String productId);


    /**
     * Check if supplier has active POs
     */
    @Query("""
        SELECT CASE WHEN EXISTS (
            SELECT 1 FROM PurchaseOrder po
            WHERE po.supplier.id = :supplierId
            AND po.status IN ('AWAITING_APPROVAL', 'DELIVERY_IN_PROCESS', 'PARTIALLY_RECEIVED')
        ) THEN true ELSE false END
    """)
    boolean existsActivePurchaseOrdersForSupplier(@Param("supplierId") Long supplierId);
}
