package com.sims.simscoreservice.product.services.searchLogic;


import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.product.enums.ProductStatus;
import com.sims.simscoreservice.product.repository.ProductRepository;
import com.sims.simscoreservice.product.services.queryService.ProductQueryService;
import com.sims.simscoreservice.product.services.searchLogic.specification.ProductSpecification;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Product Search Service
 * Handles searching and filtering of products
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {

    private final ProductRepository productRepository;
    private final ProductQueryService productQueryService;
    private final GlobalServiceHelper globalServiceHelper;

    /**
     * Search products by text (across multiple fields)
     */
    @Transactional(readOnly = true)
    public Page<Product> searchProduct(String text, String sortBy, String sortDirection, int page, int size) {
        try {
            if (text != null && !text.trim().isEmpty()) {
                Pageable pageable = globalServiceHelper.preparePageable(page, size, sortBy, sortDirection);
                return productRepository.searchProducts(text.trim().toLowerCase(), pageable);
            }

            log.info("[PM-SEARCH-LOGIC] No search text provided. Retrieving first page with default size.");
            return productQueryService.getAllProducts(sortBy, sortDirection, page, size);

        } catch (DataAccessException da) {
            log.error("[PM-SEARCH-LOGIC] Database error: {}", da.getMessage());
            throw new DatabaseException("Internal Database error", da);
        } catch (Exception e) {
            log.error("[PM-SEARCH-LOGIC] Failed to retrieve products: {}", e.getMessage());
            throw new ServiceException("Internal Service Error", e);
        }
    }

    /**
     * Filter products by various criteria
     * Supports:
     * - category:ELECTRONIC
     * - location:A1-123
     * - price:100
     * - status:ACTIVE
     * - General filter (searches across location, category, status)
     */
    @Transactional(readOnly = true)
    public Page<Product> filterProducts(String filter, String sortBy, String sortDirection, int page, int size) {
        try {
            Pageable pageable = globalServiceHelper.preparePageable(page, size, sortBy, sortDirection);

            if (filter == null || filter.trim().isEmpty()) {
                return productRepository.findAll(pageable);
            }

            Specification<Product> spec;
            String[] filterParts = filter.split(":");

            if (filterParts.length == 2) {
                // Specific field filter (field:value)
                String field = filterParts[0].toLowerCase();
                String value = filterParts[1];

                spec = switch (field) {
                    case "category" -> {
                        ProductCategories category = ProductCategories.valueOf(value.toUpperCase());
                        yield ProductSpecification.hasCategory(category);
                    }
                    case "location" -> ProductSpecification.hasLocation(value);
                    case "price" -> ProductSpecification.hasPriceLessThanOrEqual(new BigDecimal(value));
                    case "status" -> {
                        ProductStatus status = ProductStatus.valueOf(value.toUpperCase());
                        yield ProductSpecification.hasStatus(status);
                    }
                    default -> null;
                };
            } else {
                // General filter across multiple fields
                spec = ProductSpecification.generalFilter(filter.trim());
            }

            return spec != null
                    ? productRepository.findAll(spec, pageable)
                    : productRepository.findAll(pageable);

        } catch (IllegalArgumentException iae) {
            log.error("[PM-SEARCH-LOGIC] Invalid filter value: {}", iae.getMessage());
            throw new ValidationException("Invalid filter value");
        } catch (DataAccessException da) {
            log.error("[PM-SEARCH-LOGIC] Database error while filtering: {}", da.getMessage());
            throw new DatabaseException("Database error while filtering", da);
        } catch (Exception e) {
            log.error("[PM-SEARCH-LOGIC] Failed to filter products: {}", e.getMessage());
            throw new ServiceException("Internal Service Error, failed to filter products", e);
        }
    }
}
