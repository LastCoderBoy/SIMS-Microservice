package com.sims.simscoreservice.inventory.dto;

import com.sims.simscoreservice.inventory.enums.InventoryStatus;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.product.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Inventory Response DTO
 * Contains product info + inventory details
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryResponse {

    // Product info
    private String productId;
    private String productName;
    private ProductCategories category;
    private BigDecimal price;
    private ProductStatus productStatus;

    // Inventory info
    private String sku;
    private String location;
    private int currentStock;
    private int minLevel;
    private int reservedStock;
    private int availableStock;  // currentStock - reservedStock
    private InventoryStatus inventoryStatus;
    private String lastUpdate;
}
