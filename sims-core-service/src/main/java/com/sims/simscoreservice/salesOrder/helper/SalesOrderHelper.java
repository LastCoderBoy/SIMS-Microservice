package com.sims.simscoreservice.salesOrder.helper;

import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.salesOrder.dto.SalesOrderRequest;
import com.sims.simscoreservice.salesOrder.dto.SummarySalesOrderView;
import com.sims.simscoreservice.salesOrder.dto.orderItem.OrderItemRequest;
import com.sims.simscoreservice.salesOrder.entity.OrderItem;
import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.enums.OrderItemStatus;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Sales Order Helper
 * Utility methods for sales orders
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SalesOrderHelper {

    /**
     * Convert entity to summary view
     */
    public SummarySalesOrderView toSummaryView(SalesOrder order) {
        return new SummarySalesOrderView(order);
    }

    /**
     * Convert Page to PaginatedResponse
     */
    public PaginatedResponse<SummarySalesOrderView> toPaginatedSummaryView(Page<SalesOrder> salesOrderPage) {
        Page<SummarySalesOrderView> viewPage = salesOrderPage.map(this::toSummaryView);
        return new PaginatedResponse<>(viewPage);
    }


    /**
     * Validate sales order items (check for duplicates)
     */
    public void validateSalesOrderItems(List<OrderItemRequest> requestedOrderItems) {
        if (requestedOrderItems == null || requestedOrderItems.isEmpty()) {
            throw new ValidationException("Order items cannot be empty");
        }

        // Check for duplicate products in the same order
        Set<String> productIds = new HashSet<>();
        for (OrderItemRequest item : requestedOrderItems) {
            if (!productIds.add(item.getProductId())) {
                throw new ValidationException("Duplicate product found in order: " + item.getProductId());
            }
            if(item.getQuantity() <= 0) {
                throw new ValidationException("Quantity must be greater than zero");
            }
        }

        log.debug("[SO-HELPER] Sales order items validation passed");
    }

    /**
     * Validate sales order request for update
     */
    public void validateSoRequestForUpdate(SalesOrderRequest salesOrderRequest) {
        if (salesOrderRequest == null) {
            log.debug("[SO-HELPER] Sales order request is null");
            throw new ValidationException("Sales order request cannot be null");
        }
    }

    /**
     * Validate and parse sales order status string
     */
    public static SalesOrderStatus validateSalesOrderStatus(String status) {
        if (status == null) {
            return null;
        }

        try {
            return SalesOrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[SO-HELPER] Invalid status value: {}", status);
            throw new IllegalArgumentException("Invalid status value provided: " + status);
        }
    }

    /**
     * Update sales order status based on item quantities
     * - APPROVED: All items fully approved
     * - PARTIALLY_APPROVED: Some items approved
     * - PENDING: No items approved yet
     */
    public void updateSoStatusBasedOnItemQuantity(SalesOrder salesOrder) {
        boolean allApproved = allItemsFulfilled(salesOrder);
        boolean anyApproved = salesOrder.getItems().stream()
                .anyMatch(item ->
                        item.getStatus() == OrderItemStatus.APPROVED ||
                                (salesOrder.getItems().size() == 1 && item.getStatus() == OrderItemStatus.PARTIALLY_APPROVED)
                );


        if (allApproved) {
            salesOrder.setStatus(SalesOrderStatus.APPROVED);
            log.debug("[SO-HELPER] Order {} fully approved", salesOrder.getOrderReference());
        } else if (anyApproved) {
            salesOrder.setStatus(SalesOrderStatus.PARTIALLY_APPROVED);
            log.debug("[SO-HELPER] Order {} partially approved", salesOrder.getOrderReference());
        } else {
            salesOrder.setStatus(SalesOrderStatus.PENDING);
            log.debug("[SO-HELPER] Order {} still pending", salesOrder.getOrderReference());
        }
    }

    /**
     * Check if all order items are fully fulfilled
     */
    public boolean allItemsFulfilled(SalesOrder salesOrder) {
        return salesOrder.getItems().stream()
                .allMatch(item -> Objects.equals(item.getQuantity(), item.getApprovedQuantity()));
    }
}
