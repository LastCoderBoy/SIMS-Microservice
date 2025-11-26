package com.sims.simscoreservice.product.dto;

import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.product.enums.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Product Request DTO
 * Used for creating and updating products
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Location is required")
    @Pattern(regexp = "^[A-Za-z]\\d{1,2}-\\d{3}$",
            message = "Location must follow format: A1-123 (Section-Shelf)")
    private String location;

    @NotNull(message = "Category is required")
    private ProductCategories category;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "99999.99", message = "Price cannot exceed 99999.99")
    @Digits(integer = 5, fraction = 2, message = "Price must have at most 5 digits and 2 decimal places")
    private BigDecimal price;

    @NotNull(message = "Status is required")
    private ProductStatus status;
}
