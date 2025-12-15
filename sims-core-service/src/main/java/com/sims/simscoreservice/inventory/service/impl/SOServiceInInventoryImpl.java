package com.sims.simscoreservice.inventory.service.impl;

import com.sims.common.exceptions.ResourceNotFoundException;
import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.exceptions.InventoryException;
import com.sims.simscoreservice.inventory.service.SOServiceInInventory;
import com.sims.simscoreservice.salesOrder.dto.DetailedSalesOrderView;
import com.sims.simscoreservice.salesOrder.dto.ProcessSalesOrderRequestDto;
import com.sims.simscoreservice.salesOrder.dto.SummarySalesOrderView;
import com.sims.simscoreservice.salesOrder.entity.OrderItem;
import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.enums.OrderItemStatus;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import com.sims.simscoreservice.salesOrder.processor.StockOutProcessor;
import com.sims.simscoreservice.salesOrder.queryService.SalesOrderQueryService;
import com.sims.simscoreservice.salesOrder.repository.SalesOrderRepository;
import com.sims.simscoreservice.salesOrder.strategy.SalesOrderSearchService;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import com.sims.simscoreservice.stockManagement.StockManagementService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Sales Order Service in Inventory Implementation
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SOServiceInInventoryImpl implements SOServiceInInventory {

    private final Clock clock;
    private final StockOutProcessor stockOutProcessor;

    // =========== Services ===========
    private final StockManagementService stockManagementService;
    private final SalesOrderQueryService salesOrderQueryService;
    private final SalesOrderSearchService salesOrderSearchService;

    // =========== Repositories ===========
    private final SalesOrderRepository salesOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummarySalesOrderView> getAllOutgoingSalesOrders(@Min(0) int page, @Min(1) @Max(100) int size,
                                                                              String sortBy, String sortDir) {
        return salesOrderQueryService.getAllOutgoingSalesOrders(page, size, sortBy, sortDir);
    }

    @Override
    @Transactional(readOnly = true)
    public DetailedSalesOrderView getDetailsForSalesOrderId(Long orderId) {
        log.debug("[SO-INVENTORY] Getting details for sales order ID: {}", orderId);
        return salesOrderQueryService.getDetailsForSalesOrder(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummarySalesOrderView> getAllUrgentSalesOrders(@Min(0) int page, @Min(1) @Max(100) int size,
                                                                            String sortBy, String sortDir) {
        return salesOrderQueryService.getAllUrgentSalesOrders(page, size, sortBy, sortDir);
    }

    @Override
    @Transactional
    public ApiResponse<Void> processSalesOrder(ProcessSalesOrderRequestDto requestDto, String username) {
        try {
            // Find sales order
            SalesOrder salesOrder = salesOrderQueryService.findById(requestDto.getOrderId());

            // Process stock out
            SalesOrder updatedSalesOrder = stockOutProcessor.processStockOut(
                    salesOrder, requestDto.getItemQuantities(), username
            );

            // Save order
            salesOrderRepository.save(updatedSalesOrder);
            salesOrderRepository.flush();  // Populate timestamps

            log.info("[SO-INVENTORY] Sales order {} processed successfully", updatedSalesOrder.getOrderReference());

            return ApiResponse.success("Sales order processed successfully");

        } catch (InventoryException | ResourceNotFoundException exc) {
            throw exc;
        } catch (Exception e) {
            log.error("[SO-INVENTORY] Error processing order: {}", e.getMessage());
            throw new ServiceException("Failed to process order", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> cancelSalesOrder(Long orderId, String username) {
        try {
            // Find sales order
            SalesOrder salesOrder = salesOrderQueryService.findById(orderId);

            // Check if order can be cancelled
            if (salesOrder.getStatus() != SalesOrderStatus.PENDING &&
                    salesOrder.getStatus() != SalesOrderStatus.PARTIALLY_APPROVED) {
                throw new ValidationException("Only pending or partially approved orders can be cancelled");
            }

            // Release all reservations
            for (OrderItem item : salesOrder.getItems()) {
                if (! item.isFinalized()) {
                    int unreservedQuantity = item.getQuantity() - item.getApprovedQuantity();
                    if (unreservedQuantity > 0) {
                        stockManagementService.releaseReservation(
                                item.getProduct().getProductId(), unreservedQuantity
                        );
                    }
                    item.setStatus(OrderItemStatus.CANCELLED);
                }
            }

            // Update order
            salesOrder.setStatus(SalesOrderStatus.CANCELLED);
            salesOrder.setLastUpdate(GlobalServiceHelper.now(clock));
            salesOrder.setCancelledBy(username);

            salesOrderRepository.save(salesOrder);

            log.info("[SO-INVENTORY] Sales order {} cancelled successfully", orderId);

            return ApiResponse.success("Sales order cancelled successfully");

        } catch (ResourceNotFoundException | ValidationException exc) {
            throw exc;
        } catch (Exception e) {
            log.error("[SO-INVENTORY] Error cancelling order: {}", e.getMessage());
            throw new ServiceException("Failed to cancel order", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummarySalesOrderView> searchInWaitingSalesOrders(String text, int page, int size,
                                                                               String sortBy, String sortDir) {
        return salesOrderSearchService.searchPending(text, page, size, sortBy, sortDir);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummarySalesOrderView> filterWaitingSoProducts(SalesOrderStatus statusValue, String optionDateValue,
                                                                            LocalDate startDate, LocalDate endDate,
                                                                            int page, int size, String sortBy, String sortDirection) {
        return salesOrderSearchService.filterPending(statusValue, optionDateValue, startDate, endDate, page, size, sortBy, sortDirection);
    }
}
