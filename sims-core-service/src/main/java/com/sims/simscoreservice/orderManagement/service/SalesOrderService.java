package com.sims.simscoreservice.orderManagement.service;


import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.salesOrder.dto.DetailedSalesOrderView;
import com.sims.simscoreservice.salesOrder.dto.SalesOrderRequest;
import com.sims.simscoreservice.salesOrder.dto.SummarySalesOrderView;
import com.sims.simscoreservice.salesOrder.dto.orderItem.BulkOrderItemsRequestDto;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import jakarta.validation.Valid;

import java.time.LocalDate;

/**
 * Sales Order Service Interface
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface SalesOrderService {
    PaginatedResponse<SummarySalesOrderView> getAllSummarySalesOrders(String sortBy, String sortDirection, int page, int size);

    DetailedSalesOrderView getDetailsForSalesOrderId(Long orderId);

    ApiResponse<String> createSalesOrder(@Valid SalesOrderRequest salesOrderRequestDto, String jwtToken);

    ApiResponse<String> updateSalesOrder(Long orderId, SalesOrderRequest salesOrderRequestDto, String jwtToken);

    ApiResponse<String> addItemsToSalesOrder(Long orderId, @Valid BulkOrderItemsRequestDto bulkOrderItemsRequestDto, String jwtToken);

    ApiResponse<String> removeItemFromSalesOrder(Long orderId, Long itemId, String jwtToken);

    PaginatedResponse<SummarySalesOrderView> searchInSalesOrders(String text, int page, int size, String sortBy, String sortDirection);

    PaginatedResponse<SummarySalesOrderView> filterSalesOrders(SalesOrderStatus soStatus, String optionDateValue, LocalDate startDate, LocalDate endDate, int page, int size, String sortBy, String sortDirection);
}
