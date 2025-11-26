package com.sims.simscoreservice.product.entity;

import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.product.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product Entity
 * Represents products in the inventory management system
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @Column(name = "product_id", unique = true, nullable = false, length = 10)
    private String productId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // Location of the product in the shelf
    @Column(name = "location", nullable = false, length = 20)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private ProductCategories category;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Check if product is in invalid status (cannot be ordered)
     */
    public boolean isInInvalidStatus() {
        return this.status == ProductStatus.RESTRICTED ||
                this.status == ProductStatus.ARCHIVED ||
                this.status == ProductStatus.DISCONTINUED;
    }

    /**
     * Check if product is active (can be ordered)
     */
    public boolean isActive() {
        return this.status.isActive();
    }
}