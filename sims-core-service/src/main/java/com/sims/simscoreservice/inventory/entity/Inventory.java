package com.sims.simscoreservice.inventory.entity;

import com.sims.simscoreservice.inventory.enums.InventoryStatus;
import com.sims.simscoreservice.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Inventory Entity
 * Tracks stock levels and status for products
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Entity
@Table(name = "inventory")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Inventory {

    @Id
    @Column(name = "sku", unique = true, nullable = false, length = 20)
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "location", length = 20)
    private String location;

    @Column(name = "current_stock", nullable = false)
    private Integer currentStock = 0;

    @Column(name = "min_level", nullable = false)
    private Integer minLevel = 0;

    @Column(name = "reserved_stock", nullable = false)
    private Integer reservedStock = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InventoryStatus status;

    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    /**
     * Calculate available stock (current - reserved)
     */
    public int getAvailableStock() {
        return Math.max(0, currentStock - reservedStock);
    }

    /**
     * Check if stock is low (current <= min level)
     */
    public boolean isLowStock() {
        return status != InventoryStatus.INVALID && currentStock <= minLevel;
    }

    public boolean isOutOfStock() {
        return currentStock == 0;
    }
}