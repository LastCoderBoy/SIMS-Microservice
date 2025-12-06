package com.sims.simscoreservice.inventory.service;

import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.dto.InventoryPageResponse;
import com.sims.simscoreservice.inventory.dto.PendingOrderResponse;
import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.enums.InventoryStatus;
import com.sims.simscoreservice.product.entity.Product;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Inventory Service Interface
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface InventoryService {

    InventoryPageResponse getInventoryDashboard(int page, int size);

    PaginatedResponse<PendingOrderResponse> getAllPendingOrders(int page, int size);

    void addProduct(Product product, boolean isUnderTransfer);

    void saveInventoryProduct(Inventory inventory);

    void deleteByProductId(String productId);

    void updateInventoryStatus(Optional<Inventory> inventoryOpt, InventoryStatus status);

    PaginatedResponse<PendingOrderResponse> searchPendingOrders(String text, int page, int size);

    /**
     * Filter pending orders
     *
     * @param type "SALES_ORDER" or "PURCHASE_ORDER"
     * @param soStatus SalesOrderStatus (as string)
     * @param poStatus PurchaseOrderStatus (as string)
     * @param dateOption "orderDate" or "estimatedDate"
     * @param startDate start date for range filter
     * @param endDate end date for range filter
     * @param category ProductCategories (as string)
     * @param sortBy field to sort by
     * @param sortDirection "asc" or "desc"
     * @param page page number
     * @param size page size
     * @return paginated pending orders
     */
    PaginatedResponse<PendingOrderResponse> filterPendingOrders(
            String type,
            String soStatus,
            String poStatus,
            String dateOption,
            LocalDate startDate,
            LocalDate endDate,
            String category,
            String sortBy,
            String sortDirection,
            int page,
            int size
    );
}
