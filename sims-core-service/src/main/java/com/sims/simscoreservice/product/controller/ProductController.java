package com.sims.simscoreservice.product.controller;

import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.product.dto.BatchProductRequest;
import com.sims.simscoreservice.product.dto.BatchProductResponse;
import com.sims.simscoreservice.product.dto.ProductRequest;
import com.sims.simscoreservice.product.dto.ProductResponse;
import com.sims.simscoreservice.product.services.ProductService;
import com.sims.simscoreservice.shared.util.RoleValidator;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sims.common.constants.AppConstants.*;
import static com.sims.common.constants.AppConstants.DEFAULT_PAGE_NUMBER;

/**
 * Product Controller
 * REST API endpoints for product management
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RestController
@RequestMapping(BASE_PRODUCTS_PATH)
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final RoleValidator roleValidator;

    /**
     * Get all products with pagination
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[PRODUCT-CONTROLLER] Get all products requested by user: {}", userId);

        PaginatedResponse<ProductResponse> products =
                productService.getAllProducts(sortBy, sortDirection, page, size);

        return ResponseEntity.ok(products);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductResponse>> getAllProducts(@RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[PRODUCT-CONTROLLER] Get all list of products requested by user: {}", userId);

        List<ProductResponse> products = productService.getAllProducts();

        return ResponseEntity.ok(products);
    }

    /**
     * Add single product
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(
            @Valid @RequestBody ProductRequest request,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        log.info("[PRODUCT-CONTROLLER] Add product '{}' by user: {}", request.getName(), userId);

        // Check authorization (only ADMIN/MANAGER can add products)
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER"); // Throws ForbiddenException

        ProductResponse response = productService.addProduct(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product added successfully", response));
    }

    /**
     * Add multiple products in batch
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<BatchProductResponse>> addProductsBatch(
            @Valid @RequestBody BatchProductRequest request,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        log.info("[PRODUCT-CONTROLLER] Batch add {} products by user: {}", request.getProducts().size(), userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER");

        BatchProductResponse response = productService.addProductsBatch(request, userId);

        HttpStatus status = response.getFailureCount() > 0
                ? HttpStatus.MULTI_STATUS  // 207: Partial success
                : HttpStatus.CREATED;       // 201: All succeeded

        String message = String.format("Added %d/%d products successfully",
                response.getSuccessCount(),
                response.getTotalRequested());

        return ResponseEntity.status(status)
                .body(ApiResponse.success(message, response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        log.info("[PRODUCT-CONTROLLER] Update product {} by user: {}", id, userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER");

        ApiResponse<Void> response = productService.updateProduct(id.toUpperCase(), request, userId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable String id,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        log.info("[PRODUCT-CONTROLLER] Delete product {} by user: {}", id, userId);

        // Check authorization (only ADMIN can delete)
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN");

        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Product ID cannot be empty");
        }

        ApiResponse<Void> response = productService.deleteProduct(id.toUpperCase(), userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<ProductResponse>> searchProducts(
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[PRODUCT-CONTROLLER] Search products with text '{}' by user: {}", text, userId);

        PaginatedResponse<ProductResponse> results =
                productService.searchProducts(text, sortBy, sortDirection, page, size);

        return ResponseEntity.ok(results);
    }

    /**
     * Filter products
     */
    @GetMapping("/filter")
    public ResponseEntity<PaginatedResponse<ProductResponse>> filterProducts(
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String direction,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[PRODUCT-CONTROLLER] Filter products with '{}' by user: {}", filter, userId);

        PaginatedResponse<ProductResponse> results =
                productService.filterProducts(filter, sortBy, direction, page, size);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/report")
    public void generateReport(HttpServletResponse response,
                               @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[PRODUCT-CONTROLLER] Generate product report by user: {}", userId);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=products.xlsx");

        productService.generateProductReport(response);
    }
}
