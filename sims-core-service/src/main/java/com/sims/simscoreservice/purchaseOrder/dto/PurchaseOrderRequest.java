package com.sims.simscoreservice.purchaseOrder.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Purchase Order Request DTO
 * Used for creating new purchase orders
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderRequest {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotNull(message = "Ordered quantity is required")
    @Min(value = 1, message = "Order quantity must be at least 1")
    private Integer orderQuantity;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @FutureOrPresent(message = "Expected arrival date must be today or in the future")
    private LocalDate expectedArrivalDate;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
