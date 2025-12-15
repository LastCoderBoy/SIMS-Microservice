package com.sims.simscoreservice.inventory.queryService;

import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ResourceNotFoundException;
import com.sims.common.exceptions.ServiceException;
import com.sims.simscoreservice.inventory.dto.lowStock.LowStockMetrics;
import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.repository.InventoryRepository;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Inventory Query Service
 * Centralized query service for inventory read operations
 * Purpose: Prevents code duplication, breaks circular dependencies
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryQueryService {

    private final InventoryRepository inventoryRepository;
    private final GlobalServiceHelper globalServiceHelper;

    /**
     * Get inventory by SKU
     */
    @Transactional(readOnly = true)
    public Inventory getInventoryBySku(String sku) {
        return inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with SKU: " + sku));
    }

    /**
     * Get inventory by Product ID (optional)
     */
    @Transactional(readOnly = true)
    public Optional<Inventory> getInventoryByProductId(String productId) {
        return inventoryRepository.findByProductId(productId);
    }

    /**
     * Get all low stock products (paginated)
     */
    @Transactional(readOnly = true)
    public Page<Inventory> getAllLowStockProducts(String sortBy, String sortDirection, int page, int size) {
        try {
            Pageable pageable = globalServiceHelper.preparePageable(page, size, sortBy, sortDirection);
            return inventoryRepository.getLowStockItems(pageable);
        } catch (DataAccessException da) {
            log.error("[INVENTORY-QUERY] Database error retrieving low stock products: {}", da.getMessage());
            throw new DatabaseException("Failed to retrieve low stock products", da);
        } catch (Exception e) {
            log.error("[INVENTORY-QUERY] Error retrieving low stock products: {}", e.getMessage());
            throw new ServiceException("Failed to retrieve low stock products", e);
        }
    }

    /**
     * Get all low stock products (list)
     */
    @Transactional(readOnly = true)
    public List<Inventory> getAllLowStockProducts(String sortBy, String sortDirection) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortBy);

            return inventoryRepository.getLowStockItems(sort);
        } catch (DataAccessException da) {
            log.error("[INVENTORY-QUERY] Database error retrieving low stock list: {}", da.getMessage());
            throw new DatabaseException("Failed to retrieve low stock products", da);
        } catch (Exception e) {
            log.error("[INVENTORY-QUERY] Error retrieving low stock list: {}", e.getMessage());
            throw new ServiceException("Failed to retrieve low stock products", e);
        }
    }

    @Transactional(readOnly = true)
    public LowStockMetrics getLowStockMetrics() {
        try{
            return inventoryRepository.getLowStockMetrics();
        } catch (DataAccessException da) {
            log.error("[INVENTORY-QUERY] Database error retrieving low stock metrics: {}", da.getMessage());
            throw new DatabaseException("Failed to retrieve low stock metrics", da);
        } catch (Exception e) {
            log.error("[INVENTORY-QUERY] Error retrieving low stock metrics: {}", e.getMessage());
            throw new ServiceException("Failed to retrieve low stock metrics", e);
        }
    }

    /**
     * Get all inventory products (list)
     */
    @Transactional(readOnly = true)
    public List<Inventory> getAllInventoryProducts(String sortBy, String sortDirection) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortBy);

            return inventoryRepository.findAll(sort);
        } catch (DataAccessException da) {
            log.error("[INVENTORY-QUERY] Database error retrieving inventory list: {}", da.getMessage());
            throw new DatabaseException("Failed to retrieve inventory products", da);
        } catch (Exception e) {
            log.error("[INVENTORY-QUERY] Error retrieving inventory list: {}", e.getMessage());
            throw new ServiceException("Failed to retrieve inventory products", e);
        }
    }

    /**
     * Get all inventory products (paginated)
     */
    @Transactional(readOnly = true)
    public Page<Inventory> getAllInventoryProducts(String sortBy, String sortDirection, int page, int size) {
        try {
            Pageable pageable = globalServiceHelper.preparePageable(page, size, sortBy, sortDirection);
            return inventoryRepository.findAll(pageable);
        } catch (DataAccessException da) {
            log.error("[INVENTORY-QUERY] Database error retrieving inventory page: {}", da.getMessage());
            throw new DatabaseException("Failed to retrieve inventory products", da);
        } catch (Exception e) {
            log.error("[INVENTORY-QUERY] Error retrieving inventory page: {}", e.getMessage());
            throw new ServiceException("Failed to retrieve inventory products", e);
        }
    }
}