package com.sims.simscoreservice.product.repository;

import com.sims.simscoreservice.product.dto.ProductReportMetrics;
import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.product.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Product Repository
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    /**
     * Get last product ID for auto-increment
     */
    // TODO: Update if not working
    @Query("SELECT p.productId FROM Product p ORDER BY p.productId DESC LIMIT 1")
    Optional<String> findLastProductId();

    /**
     * Search products by text (across multiple fields)
     */
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.category) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(p.productId) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(p.location) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(p.status) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :text, '%'))")
    Page<Product> searchProducts(@Param("text") String text, Pageable pageable);

    /**
     * Get product metrics for reporting
     */
    @Query("""
        SELECT new com.sims.simscoreservice.product.dto.ProductReportMetrics(
            COUNT(CASE WHEN p.status IN :activeStatuses THEN 1 END),
            COUNT(CASE WHEN p.status IN :inactiveStatuses THEN 1 END)
        )
        FROM Product p
    """)
    ProductReportMetrics getProductReportMetrics(
            @Param("activeStatuses") List<ProductStatus> activeStatuses,
            @Param("inactiveStatuses") List<ProductStatus> inactiveStatuses
    );
}
