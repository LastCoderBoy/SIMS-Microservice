package com.sims.simscoreservice.inventory.repository;

import com.sims.simscoreservice.inventory.dto.InventoryMetrics;
import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.enums.InventoryStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Inventory Repository
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String>,
        JpaSpecificationExecutor<Inventory> {

    /**
     * Delete inventory by product ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Inventory i WHERE i.product.productId = :productId")
    void deleteByProductId(@Param("productId") String productId);

    /**
     * Get inventory metrics (total count, low stock count)
     */
    @Query("""
        SELECT new com.sims.simscoreservice.inventory.dto.InventoryMetrics(
            COUNT(*),
            COUNT(CASE WHEN i.currentStock <= i.minLevel AND i.status != 'INVALID' THEN 1 ELSE NULL END)
        )
        FROM Inventory i
    """)
    InventoryMetrics getInventoryMetrics();

    /**
     * Search products (SKU, Location, Product ID, Name, Category)
     */
    @Query("SELECT i FROM Inventory i WHERE " +
            "LOWER(i.sku) LIKE CONCAT('%', :text, '%') OR " +
            "LOWER(i.location) LIKE CONCAT('%', :text, '%') OR " +
            "LOWER(i.product.productId) LIKE CONCAT('%', :text, '%') OR " +
            "LOWER(i.product.name) LIKE CONCAT('%', :text, '%') OR " +
            "LOWER(i.product.category) LIKE CONCAT('%', :text, '%')")
    Page<Inventory> searchProducts(@Param("text") String text, Pageable pageable);

    /**
     * Search in low stock products only
     */
    @Query("SELECT i FROM Inventory i WHERE " +
            "i.status != 'INVALID' AND i.currentStock <= i.minLevel AND (" +
            "LOWER(i.sku) LIKE CONCAT('%', :text, '%') OR " +
            "LOWER(i.location) LIKE CONCAT('%', :text, '%') OR " +
            "LOWER(i.product.productId) LIKE CONCAT('%', :text, '%') OR " +
            "LOWER(i.product.name) LIKE CONCAT('%', :text, '%') OR " +
            "LOWER(i.product.category) LIKE CONCAT('%', :text, '%'))")
    Page<Inventory> searchInLowStockProducts(@Param("text") String text, Pageable pageable);

    Page<Inventory> findByStatus(InventoryStatus status, Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.currentStock <= :level")
    Page<Inventory> findByStockLevel(@Param("level") Integer level, Pageable pageable);

    Optional<Inventory> findBySku(String sku);

    @Query("SELECT i FROM Inventory i WHERE i.product.productId = :productId")
    Optional<Inventory> findByProductId(@Param("productId") String productId);

    /**
     * Get low stock items (list)
     */
    @Query("SELECT i FROM Inventory i WHERE i.status != 'INVALID' AND i.currentStock <= i.minLevel")
    List<Inventory> getLowStockItems(Sort sort);

    /**
     * Get low stock items (paginated)
     */
    @Query("SELECT i FROM Inventory i WHERE i.status != 'INVALID' AND i.currentStock <= i.minLevel")
    Page<Inventory> getLowStockItems(Pageable pageable);

    /**
     * Find by product ID with pessimistic write lock (for stock reservation)
     */
    @Lock(LockModeType. PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.productId = :productId")
    Inventory findByProductIdWithLock(@Param("productId") String productId);


    // ******* Report & Analytics related methods *******
//
//    @Query("""
//    SELECT new com.JK.SIMS.models.reportAnalyticsMetrics.inventoryHealth.InventoryReportMetrics(
//        CAST(COALESCE(SUM(ic.currentStock * pm.price), 0.0) AS BigDecimal),
//        CAST(COALESCE(SUM(ic.currentStock), 0) AS long),
//        CAST(COALESCE(SUM(ic.reservedStock), 0) AS long),
//        CAST(COALESCE(SUM(CASE WHEN ic.currentStock > ic.reservedStock
//            THEN ic.currentStock - ic.reservedStock
//            ELSE 0 END), 0) AS long),
//        COUNT(CASE WHEN ic.currentStock > ic.minLevel THEN 1 END),
//        COUNT(CASE WHEN ic.currentStock <= ic.minLevel AND ic.currentStock > 0 AND ic.status!='INVALID' THEN 1 END),
//        COUNT(CASE WHEN ic.currentStock = 0 THEN 1 END)
//        )
//    FROM InventoryControlData ic
//    JOIN ic.pmProduct pm
//    WHERE ic.status != 'INVALID'
//""")
//    InventoryReportMetrics getInventoryReportMetrics();
//
//    @Query(value = """
//    SELECT COALESCE(SUM(ic.current_stock * pm.price), 0) AS per_stock_value
//    FROM inventory_control_data ic
//    JOIN products_for_management pm USING(productid)
//    WHERE ic.status != 'INVALID'
//    """, nativeQuery = true)
//    BigDecimal getInventoryStockValueAtRetail();
}
