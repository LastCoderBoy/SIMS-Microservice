package com.sims.simscoreservice.inventory.service.impl;

import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.dto.InventoryMetrics;
import com.sims.simscoreservice.inventory.dto.InventoryPageResponse;
import com.sims.simscoreservice.inventory.dto.PendingOrderResponse;
import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.enums.InventoryStatus;
import com.sims.simscoreservice.inventory.helper.InventoryHelper;
import com.sims.simscoreservice.inventory.repository.InventoryRepository;
import com.sims.simscoreservice.inventory.service.InventoryService;
import com.sims.simscoreservice.inventory.searchService.InventorySearchService;
import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Inventory Service Implementation
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryHelper inventoryHelper;
    private final GlobalServiceHelper globalServiceHelper;
    private final InventorySearchService inventorySearchService;

    // TODO: Inject when implementing
    // private final SalesOrderQueryService salesOrderQueryService;
    // private final PurchaseOrderQueryService purchaseOrderQueryService;
    // private final DamageLossQueryService damageLossQueryService;


    @Override
    @Transactional(readOnly = true)
    public InventoryPageResponse getInventoryPageData(int page, int size) {
        try {
            // Get inventory metrics
            InventoryMetrics metrics = inventoryRepository.getInventoryMetrics();

            // Get pending orders (Sales + Purchase)
            PaginatedResponse<PendingOrderResponse> pendingOrders = getAllPendingOrders(page, size);

            // TODO: Get counts from other services when implemented
            Long incomingStockSize = 0L; // purchaseOrderQueryService.getTotalValidPoSize();
            Long outgoingStockSize = 0L; // salesOrderQueryService.countOutgoingSalesOrders();
            Long damageLossSize = 0L;    // damageLossQueryService. countTotalDamagedProducts();

            InventoryPageResponse response = InventoryPageResponse.builder()
                    .totalInventorySize(metrics.getTotalCount())
                    .lowStockSize(metrics.getLowStockCount())
                    .incomingStockSize(incomingStockSize)
                    .outgoingStockSize(outgoingStockSize)
                    .damageLossSize(damageLossSize)
                    .allPendingOrders(pendingOrders)
                    .build();

            log.info("[INVENTORY-SERVICE] Inventory page data retrieved: {} total, {} low stock",
                    metrics.getTotalCount(), metrics.getLowStockCount());

            return response;

        } catch (DataAccessException e) {
            log.error("[INVENTORY-SERVICE] Database error loading page data: {}", e.getMessage());
            throw new DatabaseException("Failed to load inventory page data", e);
        } catch (Exception e) {
            log.error("[INVENTORY-SERVICE] Error loading page data: {}", e.getMessage());
            throw new ServiceException("Failed to load inventory page data", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<PendingOrderResponse> getAllPendingOrders(int page, int size) {
        try {
            // TODO: Fetch pending Sales Orders
            // PaginatedResponse<SummarySalesOrderView> pendingSO =
            //     salesOrderQueryService.getAllOutgoingSalesOrders(page, size, "orderDate", "desc");

            // TODO: Fetch pending Purchase Orders
            // PaginatedResponse<SummaryPurchaseOrderView> pendingPO =
            //     purchaseOrderQueryService.getAllPendingPurchaseOrders(page, size, "product.name", "asc");

            // Combine results
            List<PendingOrderResponse> combinedResults = new ArrayList<>();

            // TODO: Add SO and PO to combined list using helper methods
            // inventoryHelper.fillWithSalesOrderView(combinedResults, pendingSO.getContent());
            // inventoryHelper.fillWithPurchaseOrderView(combinedResults, pendingPO.getContent());

            return new PaginatedResponse<>(
                    new PageImpl<>(combinedResults, PageRequest.of(page, size), combinedResults.size())
            );

        } catch (Exception e) {
            log.error("[INVENTORY-SERVICE] Error fetching pending orders: {}", e. getMessage());
            throw new ServiceException("Failed to fetch pending orders", e);
        }
    }

    @Override
    @Transactional
    public void addProduct(Product product, boolean isUnderTransfer) {
        try {
            // Generate SKU
            String sku = inventoryHelper.generateSku(product.getProductId(), product.getCategory());

            // Create inventory entry
            Inventory inventory = new Inventory();
            inventory.setSku(sku);
            inventory.setProduct(product);
            inventory.setLocation(product.getLocation());
            inventory.setCurrentStock(0);
            inventory.setMinLevel(0);
            inventory.setReservedStock(0);

            // Determine status
            InventoryStatus status = inventoryHelper.determineInventoryStatus(
                    product, 0, 0, isUnderTransfer
            );
            inventory.setStatus(status);

            inventoryRepository.save(inventory);
            inventoryRepository.flush();

            log.info("[INVENTORY-SERVICE] Product added to inventory - SKU: {}, Status: {}", sku, status);

        } catch (DataAccessException da) {
            log.error("[INVENTORY-SERVICE] Database error adding product: {}", da.getMessage());
            throw new DatabaseException("Failed to add product to inventory", da);
        } catch (Exception e) {
            log.error("[INVENTORY-SERVICE] Error adding product: {}", e.getMessage());
            throw new ServiceException("Failed to add product to inventory", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveInventoryProduct(Inventory inventory) {
        try {
            inventoryRepository.save(inventory);
            log.info("[INVENTORY-SERVICE] Saved inventory: {}", inventory.getSku());
        } catch (DataAccessException da) {
            log.error("[INVENTORY-SERVICE] Database error saving inventory: {}", da.getMessage());
            throw new DatabaseException("Failed to save inventory", da);
        } catch (Exception e) {
            log.error("[INVENTORY-SERVICE] Error saving inventory: {}", e.getMessage());
            throw new ServiceException("Failed to save inventory", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation. MANDATORY)
    public void deleteByProductId(String productId) {
        try {
            inventoryRepository.deleteByProductId(productId);
            log.info("[INVENTORY-SERVICE] Deleted inventory for product: {}", productId);
        } catch (DataAccessException da) {
            log.error("[INVENTORY-SERVICE] Database error deleting inventory: {}", da.getMessage());
            throw new DatabaseException("Failed to delete inventory", da);
        } catch (Exception e) {
            log.error("[INVENTORY-SERVICE] Error deleting inventory: {}", e.getMessage());
            throw new ServiceException("Failed to delete inventory", e);
        }
    }

    @Override
    @Transactional
    public void updateInventoryStatus(Optional<Inventory> inventoryOpt, InventoryStatus status) {
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            inventory.setStatus(status);

            try {
                inventoryRepository.save(inventory);
                log.info("[INVENTORY-SERVICE] Updated inventory status: {} -> {}",
                        inventory.getSku(), status);
            } catch (Exception e) {
                log.error("[INVENTORY-SERVICE] Error updating status: {}", e.getMessage());
                throw new ServiceException("Failed to update inventory status", e);
            }
        }
    }

    @Override
    public PaginatedResponse<PendingOrderResponse> searchPendingOrders(String text, int page, int size) {
        try {
            globalServiceHelper.validatePaginationParameters(page, size);

            Optional<String> inputText = Optional.ofNullable(text);
            if (inputText.isPresent() && !inputText.get().trim().isEmpty()) {
                log. info("[INVENTORY-SERVICE] Searching pending orders with text: '{}'", text);
                // TODO:
//                return inventorySearchService.searchInPendingOrders(text, page, size);
            }

            log.info("[INVENTORY-SERVICE] No search text, returning all pending orders");
            return getAllPendingOrders(page, size);

        } catch (IllegalArgumentException e) {
            log.error("[INVENTORY-SERVICE] Invalid pagination parameters: {}", e.getMessage());
            throw new ValidationException("Invalid pagination parameters");
        } catch (Exception e) {
            log.error("[INVENTORY-SERVICE] Error searching pending orders: {}", e.getMessage());
            throw new ServiceException("Failed to search pending orders", e);
        }
    }

    @Override
    public PaginatedResponse<PendingOrderResponse> filterPendingOrders(String type, String soStatus, String poStatus, String dateOption, LocalDate startDate, LocalDate endDate, String category, String sortBy, String sortDirection, int page, int size) {
        return null;
    }
}
