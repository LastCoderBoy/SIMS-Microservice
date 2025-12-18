package com.sims.simscoreservice.product.services;

import com.sims.simscoreservice.product.dto.*;
import com.sims.simscoreservice.product.entity.Product;
import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * Product Service Interface
 * Handles product business logic
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface ProductService {

    PaginatedResponse<ProductResponse> getAllProducts(String sortBy, String sortDirection, int page, int size);

    List<ProductResponse> getAllProducts();

    // Add single product
    ProductResponse addProduct(ProductRequest request, String userId);

    // Add multiple products in batch
    BatchProductResponse addProductsBatch(BatchProductRequest request, String userId);

    ApiResponse<Void> updateProduct(String productId, ProductRequest request, String userId);

    ApiResponse<Void> deleteProduct(String productId, String userId);

    PaginatedResponse<ProductResponse> searchProducts(String text, String sortBy, String sortDirection, int page, int size);

    PaginatedResponse<ProductResponse> filterProducts(String filter, String sortBy, String direction, int page, int size);

    void generateProductReport(HttpServletResponse response);

    // Save product (used internally by other modules)
    void saveProduct(Product product);
}
