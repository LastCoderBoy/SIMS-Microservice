package com.sims.simscoreservice.inventory.repository;


import com.sims.simscoreservice.inventory.dto.damageLoss.DamageLossMetrics;
import com.sims.simscoreservice.inventory.entity.DamageLoss;
import com.sims.simscoreservice.inventory.enums.LossReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Damage/Loss Repository
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Repository
public interface DamageLossRepository extends JpaRepository<DamageLoss, Integer> {

    /**
     * Get damage/loss metrics for the Dashboard
     */
    @Query("""
        SELECT new com.sims.simscoreservice.inventory.dto.damageLoss.DamageLossMetrics(
            COUNT(*),
            CAST(COALESCE(SUM(dl.quantityLost), 0) AS Long),
            CAST(COALESCE(SUM(dl.lossValue), 0) AS BigDecimal)
        )
        FROM DamageLoss dl
    """)
    DamageLossMetrics getDamageLossMetrics();

    /**
     * Search damage/loss reports by product name or SKU
     */
    @Query("SELECT dl FROM DamageLoss dl WHERE " +
            "LOWER(dl.inventory.product.name) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(dl.inventory.sku) LIKE LOWER(CONCAT('%', :text, '%'))")
    Page<DamageLoss> searchReports(@Param("text") String text, Pageable pageable);

    /**
     * Find by loss reason
     */
    Page<DamageLoss> findByReason(LossReason reason, Pageable pageable);



    // ******* Report & Analytics related methods *******

    @Query("SELECT COALESCE(SUM(dl.quantityLost), 0) FROM DamageLoss dl")
    Long countTotalDamagedProducts();

    /**
     * Sum loss value between start and end dates
     */
    @Query("""
        SELECT COALESCE(SUM(dl.lossValue), 0)
        FROM DamageLoss dl
        WHERE dl.lossDate BETWEEN :startDate AND :endDate
    """)
    BigDecimal sumLossValueBetween(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);
}
