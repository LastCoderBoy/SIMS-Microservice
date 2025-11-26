package com.sims.simscoreservice.product.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product Metrics DTO
 * Used for reporting & analytics
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductReportMetrics {
    private Long totalActiveProducts;
    private Long totalInactiveProducts;
}
