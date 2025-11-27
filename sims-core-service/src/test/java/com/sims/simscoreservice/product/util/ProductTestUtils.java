package com.sims.simscoreservice.product.util;

import com.sims.simscoreservice.product.dto.ProductRequest;
import com.sims.simscoreservice.product.dto.ProductResponse;
import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.product.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Test Utility for creating Product test data
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public class ProductTestUtils {
    // ========================================
    // Product Entities (for database/repository tests)
    // ========================================

    /**
     * Create Product A - Active Electronic Product
     */
    public static Product createProductA() {
        Product product = new Product();
        product.setProductId("PRD001");
        product.setName("Nintendo Switch");
        product.setLocation("A1-101");
        product.setCategory(ProductCategories.ELECTRONIC);
        product.setPrice(new BigDecimal("299.99"));
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(LocalDateTime.now(). minusDays(10));
        product.setUpdatedAt(LocalDateTime.now().minusDays(5));
        return product;
    }

    /**
     * Create Product B - Planning Educational Product
     */
    public static Product createProductB() {
        Product product = new Product();
        product.setProductId("PRD002");
        product.setName("LEGO Education Set");
        product.setLocation("B2-205");
        product.setCategory(ProductCategories.EDUCATION);
        product.setPrice(new BigDecimal("89.99"));
        product.setStatus(ProductStatus.PLANNING);
        product.setCreatedAt(LocalDateTime.now().minusDays(3));
        product.setUpdatedAt(LocalDateTime.now().minusDays(1));
        return product;
    }

    /**
     * Create Product C - On Order Action Figure
     */
    public static Product createProductC() {
        Product product = new Product();
        product.setProductId("PRD003");
        product.setName("Marvel Avengers Figure");
        product.setLocation("C3-301");
        product.setCategory(ProductCategories.ACTION_FIGURES);
        product.setPrice(new BigDecimal("24.99"));
        product.setStatus(ProductStatus.ON_ORDER);
        product.setCreatedAt(LocalDateTime.now().minusDays(7));
        product.setUpdatedAt(LocalDateTime.now().minusDays(2));
        return product;
    }

    /**
     * Create Product D - Discontinued Doll
     */
    public static Product createProductD() {
        Product product = new Product();
        product.setProductId("PRD004");
        product.setName("Barbie Dreamhouse");
        product.setLocation("D1-150");
        product.setCategory(ProductCategories.DOLLS);
        product.setPrice(new BigDecimal("199.99"));
        product.setStatus(ProductStatus.DISCONTINUED);
        product.setCreatedAt(LocalDateTime.now().minusDays(100));
        product.setUpdatedAt(LocalDateTime.now().minusDays(50));
        return product;
    }

    /**
     * Create Product E - Archived Musical Toy
     */
    public static Product createProductE() {
        Product product = new Product();
        product.setProductId("PRD005");
        product.setName("Toy Piano");
        product.setLocation("E2-220");
        product.setCategory(ProductCategories.MUSICAL_TOYS);
        product.setPrice(new BigDecimal("49.99"));
        product.setStatus(ProductStatus.ARCHIVED);
        product.setCreatedAt(LocalDateTime.now().minusDays(200));
        product.setUpdatedAt(LocalDateTime.now().minusDays(100));
        return product;
    }

    // ========================================
    // Product Requests (for API/controller tests)
    // ========================================

    /**
     * Create valid Product Request
     */
    public static ProductRequest createValidProductRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("Test Product");
        request.setLocation("A1-123");
        request.setCategory(ProductCategories.ELECTRONIC);
        request.setPrice(new BigDecimal("99.99"));
        request.setStatus(ProductStatus.ACTIVE);
        return request;
    }

    /**
     * Create Product Request with PLANNING status (no inventory)
     */
    public static ProductRequest createPlanningProductRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("Planning Product");
        request.setLocation("B2-234");
        request.setCategory(ProductCategories.EDUCATION);
        request.setPrice(new BigDecimal("49.99"));
        request.setStatus(ProductStatus.PLANNING);
        return request;
    }

    /**
     * Create invalid Product Request (missing required fields)
     */
    public static ProductRequest createInvalidProductRequest() {
        ProductRequest request = new ProductRequest();
        request. setName(""); // Invalid: empty name
        request.setLocation("INVALID"); // Invalid: wrong format
        request.setPrice(new BigDecimal("-10.00")); // Invalid: negative price
        return request;
    }

    // ========================================
    // Product Responses (for DTO mapping tests)
    // ========================================

    /**
     * Create Product Response from Product Entity
     */
    public static ProductResponse createProductResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .location(product. getLocation())
                .category(product.getCategory())
                .price(product.getPrice())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product. getUpdatedAt())
                .build();
    }
}
