package com.sims.simscoreservice.inventory.helper;

import com.sims.common.exceptions.ValidationException;
import com. sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.dto.InventoryRequest;
import com.sims.simscoreservice.inventory.dto.InventoryResponse;
import com.sims.simscoreservice.inventory.dto.PendingOrderResponse;
import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.enums.InventoryStatus;
import com.sims.simscoreservice.inventory.mapper.InventoryMapper;
import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.product.enums.ProductCategories;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory Helper
 * Utility methods for inventory operations
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryHelper {

    private final InventoryMapper inventoryMapper;

    /**
     * Validate inventory update request
     */
    public void validateUpdateRequest(InventoryRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getCurrentStock() == null && request.getMinLevel() == null) {
            errors.add("At least one of currentStock or minLevel must be provided");
        }

        if (request.getCurrentStock() != null && request.getCurrentStock() < 0) {
            errors.add("Current stock cannot be negative");
        }

        if (request.getMinLevel() != null && request.getMinLevel() < 0) {
            errors.add("Minimum level cannot be negative");
        }

        if (! errors.isEmpty()) {
            throw new ValidationException(String.join("; ", errors));
        }
    }

    /**
     * Generate SKU from product ID and category
     * Example: PRD001 + ELECTRONIC → ELE-001
     */
    public String generateSku(String productId, ProductCategories category) {
        try {
            if (productId == null || productId.length() < 4) {
                throw new IllegalArgumentException("Product ID must be at least 4 characters (e.g., PRD001)");
            }
            if (category == null) {
                throw new IllegalArgumentException("Category cannot be null");
            }

            String lastDigits = productId.substring(3);  // PRD001 → 001
            String categoryPrefix = category.toString().substring(0, 3);  // ELECTRONIC → ELE
            return categoryPrefix + "-" + lastDigits;  // ELE-001

        } catch (Exception e) {
            log.error("[INVENTORY-HELPER] Failed to generate SKU: {}", e.getMessage());
            throw new ValidationException("Failed to generate SKU: " + e.getMessage());
        }
    }

    /**
     * Convert Page<Inventory> to PaginatedResponse<InventoryResponse>
     */
    public PaginatedResponse<InventoryResponse> toPaginatedResponse(Page<Inventory> inventoryPage) {
        List<InventoryResponse> content = inventoryMapper.toResponseList(inventoryPage.getContent());

        return PaginatedResponse.<InventoryResponse>builder()
                .content(content)
                .totalPages(inventoryPage.getTotalPages())
                .totalElements(inventoryPage.getTotalElements())
                .currentPage(inventoryPage.getNumber())
                .pageSize(inventoryPage.getSize())
                .build();
    }

    /**
     * Determine inventory status based on product status and stock levels
     */
    public InventoryStatus determineInventoryStatus(Product product, int currentStock, int minLevel, boolean isUnderTransfer) {
        // If product is invalid (restricted, archived, discontinued)
        if (product.isInInvalidStatus()) {
            return InventoryStatus.INVALID;
        }

        // If product is incoming from supplier
        if (isUnderTransfer) {
            return InventoryStatus.INCOMING;
        }

        // Check stock levels
        if (currentStock <= minLevel) {
            return InventoryStatus.LOW_STOCK;
        }

        return InventoryStatus.IN_STOCK;
    }

    /**
     * Create PendingOrderResponse from Purchase Order data
     * (Will be used when PO integration is complete)
     */
    public PendingOrderResponse createPendingOrderFromPO(Long id, String poNumber, String status,
                                                         String supplierName, Integer quantity,
                                                         LocalDate orderDate, LocalDate expectedArrivalDate) {
        return PendingOrderResponse.builder()
                .id(id)
                .orderReference(poNumber)
                .type("PURCHASE_ORDER")
                .status(status)
                .customerOrSupplierName(supplierName)
                .totalOrderedQuantity(quantity)
                . orderDate(orderDate != null ? orderDate. atStartOfDay() : null)
                .estimatedDate(expectedArrivalDate != null ? expectedArrivalDate.atStartOfDay() : null)
                .build();
    }

    /**
     * Create PendingOrderResponse from Sales Order data
     * (Will be used when SO integration is complete)
     */
    public PendingOrderResponse createPendingOrderFromSO(Long id, String orderRef, String status,
                                                         String customerName, Integer quantity,
                                                         LocalDateTime orderDate, LocalDateTime estimatedDeliveryDate) {
        return PendingOrderResponse.builder()
                .id(id)
                .orderReference(orderRef)
                .type("SALES_ORDER")
                .status(status)
                .customerOrSupplierName(customerName)
                .totalOrderedQuantity(quantity)
                .orderDate(orderDate)
                .estimatedDate(estimatedDeliveryDate)
                .build();
    }

//
//    public void fillWithPurchaseOrders(List<PendingOrderResponse> combinedPendingOrders,
//                                       List<PurchaseOrder> pendingPurchaseOrders){
//        for(PurchaseOrder po : pendingPurchaseOrders){
//            PendingOrderResponse pendingOrder = new PendingOrderResponse(
//                    po.getId(),
//                    po.getPONumber(),
//                    StockMovementReferenceType.PURCHASE_ORDER.toString(),
//                    po.getStatus().toString(),
//                    po.getOrderDate() != null ? po.getOrderDate().atStartOfDay() : null,
//                    po.getExpectedArrivalDate() != null ? po.getExpectedArrivalDate().atStartOfDay() : null,
//                    po.getSupplier().getName(),
//                    po.getOrderedQuantity()
//            );
//            combinedPendingOrders.add(pendingOrder);
//        }
//    }
//
//    public void fillWithPurchaseOrderView(List<PendingOrderResponse> combinedPendingOrders,
//                                          List<SummaryPurchaseOrderView> pendingPurchaseOrders){
//        for(SummaryPurchaseOrderView po : pendingPurchaseOrders){
//            PendingOrderResponse pendingOrder = new PendingOrderResponse(
//                    po.getId(),
//                    po.getPoNumber(),
//                    StockMovementReferenceType.PURCHASE_ORDER.toString(),
//                    po.getStatus().toString(),
//                    po.getOrderDate() != null ? po.getOrderDate().atStartOfDay() : null,
//                    po.getExpectedArrivalDate() != null ? po.getExpectedArrivalDate().atStartOfDay() : null,
//                    po.getSupplierName(),
//                    po.getOrderedQuantity()
//            );
//            combinedPendingOrders.add(pendingOrder);
//        }
//    }
//
//    public void fillWithSalesOrders(List<PendingOrdersResponseInIC> combinedPendingOrders,
//                                    List<SalesOrder> pendingSalesOrders){
//        pendingSalesOrders.forEach(so ->
//                combinedPendingOrders.add(new PendingOrderResponse(
//                        so.getId(),
//                        so.getOrderReference(),
//                        StockMovementReferenceType.SALES_ORDER.toString(),
//                        so.getStatus().toString(),
//                        so.getOrderDate(),
//                        so.getEstimatedDeliveryDate(),
//                        so.getCustomerName(),
//                        so.getItems().stream().mapToInt(OrderItem::getQuantity).sum()
//                ))
//        );
//    }
//
//    public void fillWithSalesOrderView(List<PendingOrdersResponseInIC> combinedPendingOrders,
//                                       List<SummarySalesOrderView> pendingSalesOrders){
//        pendingSalesOrders.forEach(so ->
//                combinedPendingOrders.add(new PendingOrdersResponseInIC(
//                        so.getId(),
//                        so.getOrderReference(),
//                        StockMovementReferenceType.SALES_ORDER.toString(),
//                        so.getStatus().toString(),
//                        so.getOrderDate(),
//                        so.getEstimatedDeliveryDate(),
//                        so.getCustomerName(),
//                        so.getTotalOrderedQuantity()
//                ))
//        );
//    }
}
