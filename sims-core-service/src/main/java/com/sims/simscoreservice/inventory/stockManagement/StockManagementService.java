package com.sims.simscoreservice.inventory.stockManagement;

import com.sims.common.exceptions.*;
import com.sims.simscoreservice.exceptions.InsufficientStockException;
import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.repository.InventoryRepository;
import com.sims.simscoreservice.inventory.service.InventoryStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework. stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stock Management Service
 * Handles stock reservations, fulfillment, and releases
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockManagementService {

    private final InventoryRepository inventoryRepository;
    private final InventoryStatusService inventoryStatusService;

    /**
     * Reserve stock atomically (with pessimistic lock)
     * Used when creating Sales Order
     *
     * @param productId Product ID
     * @param requestedQuantity Quantity to reserve
     * @throws InsufficientStockException if not enough stock available
     */
    @Transactional
    public void reserveStock(String productId, Integer requestedQuantity) {
        try {
            // Lock row for update
            Inventory inventory = inventoryRepository.findByProductIdWithLock(productId);
            if (inventory == null) {
                throw new ResourceNotFoundException("Inventory not found for product: " + productId);
            }

            int availableStock = inventory.getAvailableStock();

            if (availableStock < requestedQuantity) {
                log.warn("[STOCK-MGMT] Insufficient stock for product {}. Available: {}, Requested: {}",
                        productId, availableStock, requestedQuantity);
                throw new InsufficientStockException(
                        String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                                productId, availableStock, requestedQuantity));
            }

            // Reserve stock
            inventory.setReservedStock(inventory.getReservedStock() + requestedQuantity);
            inventoryRepository.save(inventory);

            log.info("[STOCK-MGMT] Reserved {} units for product {}", requestedQuantity, productId);

        } catch (InsufficientStockException | ResourceNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log. error("[STOCK-MGMT] Database error reserving stock: {}", e.getMessage());
            throw new DatabaseException("Failed to reserve stock", e);
        } catch (Exception e) {
            log.error("[STOCK-MGMT] Unexpected error reserving stock: {}", e.getMessage());
            throw new ServiceException("Failed to reserve stock", e);
        }
    }

    /**
     * Fulfill reservation (deduct from both current and reserved stock)
     * Used when Sales Order is fulfilled/shipped
     *
     * @param productId Product ID
     * @param approvedQuantity Quantity to fulfill
     */
    @Transactional
    public void fulfillReservation(String productId, int approvedQuantity) {
        try {
            Inventory inventory = inventoryRepository.findByProductIdWithLock(productId);

            if (inventory == null) {
                throw new ResourceNotFoundException("Inventory not found for product: " + productId);
            }

            if (approvedQuantity > inventory.getReservedStock()) {
                throw new ValidationException(
                        "Cannot fulfill more than reserved quantity. Reserved: " + inventory.getReservedStock());
            }

            // Deduct from both current and reserved
            inventory.setCurrentStock(inventory.getCurrentStock() - approvedQuantity);
            inventory.setReservedStock(inventory.getReservedStock() - approvedQuantity);

            // Update status (might become LOW_STOCK)
            inventoryStatusService.updateInventoryStatus(inventory);
            inventoryRepository.save(inventory);

            log.info("[STOCK-MGMT] Fulfilled {} units for product {}", approvedQuantity, productId);

        } catch (ResourceNotFoundException | ValidationException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("[STOCK-MGMT] Database error fulfilling reservation: {}", e.getMessage());
            throw new DatabaseException("Failed to fulfill reservation", e);
        } catch (Exception e) {
            log.error("[STOCK-MGMT] Unexpected error fulfilling reservation: {}", e.getMessage());
            throw new ServiceException("Failed to fulfill reservation", e);
        }
    }

    /**
     * Release reservation (when order is cancelled)
     *
     * @param productId Product ID
     * @param releasedQuantity Quantity to release
     */
    @Transactional
    public void releaseReservation(String productId, int releasedQuantity) {
        try {
            Inventory inventory = inventoryRepository.findByProductIdWithLock(productId);

            if (inventory == null) {
                throw new ResourceNotFoundException("Inventory not found for product: " + productId);
            }

            if (inventory.getReservedStock() < releasedQuantity) {
                log.warn("[STOCK-MGMT] Attempting to release {} but only {} reserved for product {}",
                        releasedQuantity, inventory.getReservedStock(), productId);
            }

            // Release reservation (don't go below 0)
            inventory.setReservedStock(Math.max(0, inventory.getReservedStock() - releasedQuantity));
            inventoryRepository.save(inventory);

            log.info("[STOCK-MGMT] Released {} units for product {}", releasedQuantity, productId);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("[STOCK-MGMT] Database error releasing reservation: {}", e. getMessage());
            throw new DatabaseException("Failed to release reservation", e);
        } catch (Exception e) {
            log.error("[STOCK-MGMT] Unexpected error releasing reservation: {}", e. getMessage());
            throw new ServiceException("Failed to release reservation", e);
        }
    }

    /**
     * Update stock levels
     *
     * @param inventory Inventory to update
     * @param newCurrentStock New current stock (optional)
     * @param newMinLevel New min level (optional)
     */
    @Transactional
    public void updateStockLevels(Inventory inventory, Integer newCurrentStock, Integer newMinLevel) {
        // Update current stock if provided
        if (newCurrentStock != null) {
            inventory.setCurrentStock(newCurrentStock);
        }

        // Update min level if provided
        if (newMinLevel != null) {
            inventory.setMinLevel(newMinLevel);
        }

        // Update status based on new levels
        inventoryStatusService.updateInventoryStatus(inventory);
        inventoryRepository.save(inventory);
    }
}
