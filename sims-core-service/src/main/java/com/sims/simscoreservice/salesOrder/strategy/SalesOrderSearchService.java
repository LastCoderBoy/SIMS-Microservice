package com.sims.simscoreservice.salesOrder.strategy;

import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.salesOrder.dto.SummarySalesOrderView;
import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import com.sims.simscoreservice.salesOrder.helper.SalesOrderHelper;
import com.sims.simscoreservice.salesOrder.queryService.SalesOrderQueryService;
import com.sims.simscoreservice.salesOrder.strategy.filterStrategy.SoFilterStrategy;
import com.sims.simscoreservice.salesOrder.strategy.searchStrategy.SoSearchStrategy;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Sales Order Search Service
 * Handles searching and filtering of sales orders
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SalesOrderSearchService {

    private final GlobalServiceHelper globalServiceHelper;
    private final SalesOrderQueryService salesOrderQueryService;
    private final SalesOrderHelper salesOrderHelper;

    // ============= Search strategies =============
    private final SoSearchStrategy icSoSearchStrategy;
    private final SoSearchStrategy omSoSearchStrategy;

    // ============= Filter strategies =============
    private final SoFilterStrategy filterWaitingSalesOrders;
    private final SoFilterStrategy filterSalesOrdersInOm;

    /**
     * Search pending sales orders (IC context)
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<SummarySalesOrderView> searchPending(String text, int page, int size, String sortBy, String sortDir) {
        try {
            globalServiceHelper.validatePaginationParameters(page, size);

            // If no search text, return all outgoing orders
            if (text == null || text.trim().isEmpty()) {
                log.warn("[SO-SEARCH] No search text, returning all pending orders");
                return salesOrderQueryService.getAllOutgoingSalesOrders(page, size, "id", "asc");
            }

            // Search pending orders
            Page<SalesOrder> salesOrderPage = icSoSearchStrategy.searchInSo(text, page, size, sortBy, sortDir);
            return salesOrderHelper.toPaginatedSummaryView(salesOrderPage);

        } catch (IllegalArgumentException ie) {
            log.error("[SO-SEARCH] Invalid pagination parameters: {}", ie.getMessage());
            throw ie;
        } catch (Exception e) {
            log.error("[SO-SEARCH] Error searching pending orders: {}", e.getMessage());
            throw new ServiceException("Failed to search orders", e);
        }
    }

    /**
     * Filter pending sales orders (IC context)
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<SummarySalesOrderView> filterPending(SalesOrderStatus statusValue, String optionDateValue,
                                                                  LocalDate startDate, LocalDate endDate,
                                                                  int page, int size, String sortBy, String sortDirection) {
        try {
            Pageable pageable = globalServiceHelper.preparePageable(page, size, sortBy, sortDirection);

             Page<SalesOrder> salesOrderPage = filterWaitingSalesOrders.filterSalesOrders(
                 statusValue, optionDateValue, startDate, endDate, pageable
             );

             return salesOrderHelper.toPaginatedSummaryView(salesOrderPage);

        } catch (IllegalArgumentException e) {
            log.error("[SO-SEARCH] filterPending() - Invalid filter parameters: {}", e.getMessage());
            throw new ValidationException("Invalid filter parameters: " + e.getMessage());
        } catch (Exception e) {
            log.error("[SO-SEARCH] Error filtering pending orders: {}", e.getMessage());
            throw new ServiceException("Failed to filter orders", e);
        }
    }

    /**
     * Search all sales orders (OM context)
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<SummarySalesOrderView> searchAll(String text, int page, int size, String sortBy, String sortDirection) {
        try {
            globalServiceHelper.validatePaginationParameters(page, size);

            if (text == null || text.trim().isEmpty()) {
                log.info("[SO-SEARCH] No search text, returning all orders");
                return salesOrderQueryService.getAllSummarySalesOrders(sortBy, sortDirection, page, size);
            }

            Page<SalesOrder> salesOrderPage = omSoSearchStrategy.searchInSo(text, page, size, sortBy, sortDirection);
            return salesOrderHelper.toPaginatedSummaryView(salesOrderPage);

        } catch (Exception e) {
            log.error("[SO-SEARCH] Error searching all orders: {}", e.getMessage());
            throw new ServiceException("Failed to search orders", e);
        }
    }

    /**
     * Filter all sales orders (OM context)
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<SummarySalesOrderView> filterAll(SalesOrderStatus statusValue, String optionDateValue,
                                                              LocalDate startDate, LocalDate endDate,
                                                              int page, int size, String sortBy, String sortDirection) {
        try {
            Pageable pageable = globalServiceHelper.preparePageable(page, size, sortBy, sortDirection);

             Page<SalesOrder> salesOrderPage = filterSalesOrdersInOm.filterSalesOrders(
                 statusValue, optionDateValue, startDate, endDate, pageable
             );
             return salesOrderHelper.toPaginatedSummaryView(salesOrderPage);

        } catch (IllegalArgumentException e) {
            log.error("[SO-SEARCH] filterAll() - Invalid filter parameters: {}", e.getMessage());
            throw new ValidationException("Invalid filter parameters: " + e.getMessage());
        } catch (Exception e) {
            log.error("[SO-SEARCH] Error filtering all orders: {}", e.getMessage());
            throw new ServiceException("Failed to filter orders", e);
        }
    }
}
