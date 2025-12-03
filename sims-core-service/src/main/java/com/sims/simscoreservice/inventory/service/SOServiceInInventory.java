package com.sims.simscoreservice.inventory.service;


import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.salesOrder.dto.ProcessSalesOrderRequestDto;
import com.sims.simscoreservice.salesOrder.dto.SummarySalesOrderView;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

/**
 * Sales Order Service in Inventory
 * Manages pending sales orders in the Inventory Context
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface SOServiceInInventory {

    PaginatedResponse<SummarySalesOrderView> getAllOutgoingSalesOrders(@Min(0) int page, @Min(1) @Max(100) int size,
                                                                       String sortBy, String sortDir);

    /**
     * Get all urgent sales orders (delivery < 2 days)
     */
    PaginatedResponse<SummarySalesOrderView> getAllUrgentSalesOrders(@Min(0) int page, @Min(1) @Max(100) int size,
                                                                     String sortBy, String sortDir);

    /**
     * Process sales order (Stock OUT)
     */
    ApiResponse<Void> processSalesOrder(ProcessSalesOrderRequestDto requestDto, String username);

    ApiResponse<Void> cancelSalesOrder(Long orderId, String username);

    PaginatedResponse<SummarySalesOrderView> searchInWaitingSalesOrders(String text, int page, int size,
                                                                        String sortBy, String sortDir);

    PaginatedResponse<SummarySalesOrderView> filterWaitingSoProducts(SalesOrderStatus statusValue, String optionDateValue,
                                                                     LocalDate startDate, LocalDate endDate,
                                                                     int page, int size, String sortBy, String sortDirection);
}
