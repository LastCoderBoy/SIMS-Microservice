package com.sims.simscoreservice.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Batch Product Response DTO
 * Contains results of batch product creation
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchProductResponse {
    private int totalRequested;
    private int successCount;
    private int failureCount;
    private List<String> successfulProductIds;
    private List<ProductError> errors;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductError {
        private int index;
        private ProductRequest product;
        private String errorMessage;
    }
}
