package com.sims.simscoreservice.inventory.searchService;


import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.dto.PendingOrderResponse;
import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.enums.InventoryStatus;
import com.sims.simscoreservice.inventory.helper.InventoryHelper;
import com.sims.simscoreservice.inventory.repository.InventoryRepository;
import com.sims.simscoreservice.inventory.queryService.InventoryQueryService;
import com.sims.simscoreservice.inventory.specification.InventorySpecification;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.purchaseOrder.dto.SummaryPurchaseOrderView;
import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;
import com.sims.simscoreservice.purchaseOrder.queryService.PurchaseOrderQueryService;
import com.sims.simscoreservice.purchaseOrder.strategy.PurchaseOrderSearchService;
import com.sims.simscoreservice.purchaseOrder.strategy.searchStrategy.PoSearchStrategy;
import com.sims.simscoreservice.salesOrder.dto.SummarySalesOrderView;
import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import com.sims.simscoreservice.salesOrder.queryService.SalesOrderQueryService;
import com.sims.simscoreservice.salesOrder.strategy.SalesOrderSearchService;
import com.sims.simscoreservice.salesOrder.strategy.searchStrategy.SoSearchStrategy;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.sims.common.constants.AppConstants.*;

/**
 * Inventory Search Service
 * Handles searching and filtering of inventory
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventorySearchService {

    // ========== Components ==========
    private final InventoryRepository inventoryRepository;
    private final InventoryQueryService inventoryQueryService;
    private final GlobalServiceHelper globalServiceHelper;
    private final InventoryHelper inventoryHelper;

    // =========== Search strategies ===========
    private final SalesOrderQueryService salesOrderQueryService;
    private final SalesOrderSearchService salesOrderSearchService;
    private final PurchaseOrderQueryService purchaseOrderQueryService;
    private final PurchaseOrderSearchService purchaseOrderSearchService;

    private final PoSearchStrategy icPoSearchStrategy;
    private final SoSearchStrategy icSoSearchStrategy;

    /**
     * Search in low stock products
     */
    @Transactional(readOnly = true)
    public Page<Inventory> searchInLowStockProducts(String text, int page, int size, String sortBy, String sortDirection) {
        try {
            Optional<String> inputText = Optional.ofNullable(text);

            if (inputText.isPresent() && !inputText.get().trim().isEmpty()) {
                Pageable pageable = globalServiceHelper.preparePageable(page, size, sortBy, sortDirection);
                return inventoryRepository.searchInLowStockProducts(inputText.get(). trim(). toLowerCase(), pageable);
            }

            log.info("[INVENTORY-SEARCH] No search text, returning all low stock items");
            return inventoryQueryService.getAllLowStockProducts(sortBy, sortDirection, page, size);

        } catch (DataAccessException e) {
            log.error("[INVENTORY-SEARCH] Database error searching low stock: {}", e.getMessage());
            throw new DatabaseException("Failed to search low stock products", e);
        } catch (Exception e) {
            log.error("[INVENTORY-SEARCH] Error searching low stock: {}", e.getMessage());
            throw new ServiceException("Failed to search low stock products", e);
        }
    }

    /**
     * Filter low stock products by category
     */
    @Transactional(readOnly = true)
    public Page<Inventory> filterLowStockProducts(ProductCategories category, String sortBy,
                                                  String sortDirection, int page, int size) {
        try {
            Pageable pageable = globalServiceHelper. preparePageable(page, size, sortBy, sortDirection);

            Specification<Inventory> spec = Specification
                    .where(InventorySpecification.hasLowStock())
                    .and(InventorySpecification.hasProductCategory(category));

            return inventoryRepository.findAll(spec, pageable);

        } catch (IllegalArgumentException iae) {
            log.error("[INVENTORY-SEARCH] Invalid filter value: {}", iae.getMessage());
            throw new ValidationException("Invalid filter value: " + iae.getMessage());
        } catch (DataAccessException da) {
            log.error("[INVENTORY-SEARCH] Database error filtering low stock: {}", da.getMessage());
            throw new DatabaseException("Failed to filter low stock products", da);
        } catch (Exception e) {
            log.error("[INVENTORY-SEARCH] Error filtering low stock: {}", e.getMessage());
            throw new ServiceException("Failed to filter low stock products", e);
        }
    }

    /**
     * Search all inventory products
     */
    @Transactional(readOnly = true)
    public Page<Inventory> searchAll(String text, String sortBy, String sortDirection, int page, int size) {
        try {
            Optional<String> inputText = Optional.ofNullable(text);

            if (inputText.isPresent() && !inputText.get(). trim().isEmpty()) {
                Pageable pageable = globalServiceHelper.preparePageable(page, size, sortBy, sortDirection);
                return inventoryRepository.searchProducts(inputText.get().trim(). toLowerCase(), pageable);
            }

            log.info("[INVENTORY-SEARCH] No search text, returning all products");
            return inventoryQueryService.getAllInventoryProducts(sortBy, sortDirection, page, size);

        } catch (DataAccessException e) {
            log.error("[INVENTORY-SEARCH] Database error searching inventory: {}", e.getMessage());
            throw new DatabaseException("Failed to search inventory", e);
        } catch (Exception e) {
            log.error("[INVENTORY-SEARCH] Error searching inventory: {}", e.getMessage());
            throw new ServiceException("Failed to search inventory", e);
        }
    }

    /**
     * Filter all inventory products
     * Supports:
     * - status:IN_STOCK
     * - status:LOW_STOCK
     * - stock:50 (current stock <= 50)
     * - ELECTRONIC (category name directly)
     */
    @Transactional(readOnly = true)
    public Page<Inventory> filterAll(String filterBy, String sortBy, String sortDirection, int page, int size) {
        try {
            Pageable pageable = globalServiceHelper.preparePageable(page, size, sortBy, sortDirection);

            if (filterBy == null || filterBy.trim().isEmpty()) {
                return inventoryRepository.findAll(pageable);
            }

            String[] filterParts = filterBy.trim().split(":");
            Page<Inventory> resultPage;

            if (filterParts. length == 2) {
                // Field-specific filter (field:value)
                String field = filterParts[0]. toLowerCase();
                String value = filterParts[1];

                resultPage = switch (field) {
                    case "status" -> {
                        InventoryStatus status = InventoryStatus.valueOf(value.toUpperCase());
                        yield inventoryRepository.findByStatus(status, pageable);
                    }
                    case "stock" -> inventoryRepository.findByStockLevel(Integer.parseInt(value), pageable);
                    default -> inventoryRepository.findAll(pageable);
                };
            } else {
                // General filter (status or category name)
                boolean isStatusType = GlobalServiceHelper.isInEnum(filterBy.trim(). toUpperCase(), InventoryStatus.class);

                Specification<Inventory> specification;
                if (isStatusType) {
                    InventoryStatus statusValue = InventoryStatus.valueOf(filterBy.trim().toUpperCase());
                    specification = Specification.where(InventorySpecification.hasStatus(statusValue));
                } else {
                    ProductCategories categoryValue = ProductCategories.valueOf(filterBy.trim().toUpperCase());
                    specification = Specification.where(InventorySpecification.hasProductCategory(categoryValue));
                }

                resultPage = inventoryRepository.findAll(specification, pageable);
            }

            return resultPage;

        } catch (IllegalArgumentException iae) {
            log.error("[INVENTORY-SEARCH] Invalid filter parameter: {}", iae.getMessage());
            throw new ValidationException("Invalid filter parameter: " + iae.getMessage());
        } catch (DataAccessException da) {
            log.error("[INVENTORY-SEARCH] Database error filtering inventory: {}", da.getMessage());
            throw new DatabaseException("Failed to filter inventory", da);
        } catch (Exception e) {
            log.error("[INVENTORY-SEARCH] Error filtering inventory: {}", e.getMessage());
            throw new ServiceException("Failed to filter inventory", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<PendingOrderResponse> searchInPendingOrders(String text, int page, int size) {
        try {
            // Search in Sales Orders
            Page<SalesOrder> salesOrderPage =
                    icSoSearchStrategy.searchInSo(text, page, size, DEFAULT_SORT_BY_FOR_SO, DEFAULT_SORT_DIRECTION);

            // Search in Purchase Orders
            Page<PurchaseOrder> purchaseOrderPage =
                    icPoSearchStrategy.searchInPo(text, page, size, DEFAULT_SORT_BY_FOR_PO, DEFAULT_SORT_DIRECTION);

            // Combine the results
            List<PendingOrderResponse> combinedResults = new ArrayList<>();
            inventoryHelper.fillWithSalesOrders(combinedResults, salesOrderPage.getContent());
            inventoryHelper.fillWithPurchaseOrders(combinedResults, purchaseOrderPage.getContent());

            // Return with correct pagination metadata
            long totalResults = salesOrderPage.getTotalElements() + purchaseOrderPage.getTotalElements();
            log.info("[INVENTORY-SEARCH] searchInPendingOrders() returning {} results", combinedResults.size());
            return new PageImpl<>(
                    combinedResults,
                    PageRequest.of(page, size),
                    totalResults
            );
        } catch (Exception e) {
            log.error("[INVENTORY-SEARCH] searchInPendingOrders(): Error searching pending orders", e);
            throw new ServiceException("Failed to search pending orders", e);
        }
    }

    /**
     * Filter pending orders (SO + PO)
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<PendingOrderResponse> filterPendingOrders(String type, SalesOrderStatus soStatus,
                                                                       PurchaseOrderStatus poStatus, String dateOption,
                                                                       LocalDate startDate, LocalDate endDate,
                                                                       ProductCategories category, String sortBy,
                                                                       String sortDirection, int page, int size) {
        try {
            List<PendingOrderResponse> combinedResults = new ArrayList<>();

            // If no filters, return all pending orders
            if (type == null && soStatus == null && poStatus == null && category == null && dateOption == null) {
                PaginatedResponse<SummarySalesOrderView> salesOrders =
                        salesOrderQueryService.getAllOutgoingSalesOrders(page, size, sortBy, sortDirection);
                inventoryHelper.fillWithSalesOrderView(combinedResults, salesOrders.getContent());

                PaginatedResponse<SummaryPurchaseOrderView> purchaseOrders =
                        purchaseOrderQueryService.getAllPendingPurchaseOrders(page, size, sortBy, sortDirection);
                inventoryHelper.fillWithPurchaseOrderView(combinedResults, purchaseOrders. getContent());
            } else {
                // Handle Sales Orders
                if ("SALES_ORDER".equalsIgnoreCase(type) || soStatus != null || (dateOption != null && poStatus == null)) {
                    PaginatedResponse<SummarySalesOrderView> salesOrders =
                            salesOrderSearchService.filterPending(soStatus, dateOption, startDate, endDate, page, size, sortBy, sortDirection);
                    inventoryHelper.fillWithSalesOrderView(combinedResults, salesOrders.getContent());
                }

                // Handle Purchase Orders
                if ("PURCHASE_ORDER".equalsIgnoreCase(type) || poStatus != null || category != null) {
                    PaginatedResponse<SummaryPurchaseOrderView> purchaseOrders =
                            purchaseOrderSearchService.filterPending(poStatus, category, sortBy, sortDirection, page, size);
                    inventoryHelper. fillWithPurchaseOrderView(combinedResults, purchaseOrders.getContent());
                }
            }

            // Sort combined results
            combinedResults.sort(Comparator.comparing(PendingOrderResponse::getOrderDate).reversed());

            log.info("[INVENTORY-SEARCH] Filter returned {} results", combinedResults.size());

            return PaginatedResponse.<PendingOrderResponse>builder()
                    .content(combinedResults)
                    .totalPages((int) Math.ceil((double) combinedResults.size() / size))
                    .totalElements(combinedResults.size())
                    .currentPage(page)
                    .pageSize(size)
                    .build();

        } catch (Exception e) {
            log.error("[INVENTORY-SEARCH] Error filtering pending orders: {}", e.getMessage());
            throw new ServiceException("Failed to filter pending orders", e);
        }
    }
}
