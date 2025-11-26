package com.sims.simscoreservice.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Batch Product Request DTO
 * Used for adding multiple products at once
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchProductRequest {

    @NotEmpty(message = "Products list cannot be empty")
    @Size(min = 1, max = 100, message = "Batch size must be between 1 and 100 products")
    @Valid
    private List<ProductRequest> products;
}
