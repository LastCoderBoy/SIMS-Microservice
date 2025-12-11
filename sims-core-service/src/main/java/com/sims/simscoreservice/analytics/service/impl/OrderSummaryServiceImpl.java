package com.sims.simscoreservice.analytics.service.impl;

import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ServiceException;
import com.sims.simscoreservice.analytics.dto.OrderSummaryMetrics;
import com.sims.simscoreservice.analytics.dto.PurchaseOrderSummary;
import com.sims.simscoreservice.analytics.dto.SalesOrderSummary;
import com.sims.simscoreservice.analytics.service.OrderSummaryService;
import com.sims.simscoreservice.purchaseOrder.repository.PurchaseOrderRepository;
import com.sims.simscoreservice.salesOrder.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Order Summary Service Implementation
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSummaryServiceImpl implements OrderSummaryService {

    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public OrderSummaryMetrics getOrderSummaryMetrics() {
        try {
            log.info("[ANALYTICS-ORDER] Fetching order summary metrics");

            SalesOrderSummary salesSummary = getSalesOrderSummary();
            PurchaseOrderSummary purchaseSummary = getPurchaseOrderSummary();

            log.info("[ANALYTICS-ORDER] Sales Orders: {} total, {}% completion rate",
                    salesSummary.getTotalOrders(),
                    String.format("%.2f", salesSummary.getCompletionRate()));

            log.info("[ANALYTICS-ORDER] Purchase Orders: {} total, {}% success rate",
                    purchaseSummary.getTotalOrders(),
                    String.format("%.2f", purchaseSummary.getSuccessRate()));

            return OrderSummaryMetrics.builder()
                    .salesOrderSummary(salesSummary)
                    .purchaseOrderSummary(purchaseSummary)
                    .build();

        } catch (DataAccessException e) {
            log.error("[ANALYTICS-ORDER] Database error:  {}", e.getMessage(), e);
            throw new DatabaseException("Failed to fetch order summary", e);
        } catch (Exception e) {
            log.error("[ANALYTICS-ORDER] Unexpected error: {}", e.getMessage(), e);
            throw new ServiceException("Failed to fetch order summary", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderSummary getSalesOrderSummary() {
        try {
            log.debug("[ANALYTICS-ORDER] Fetching sales order summary");
            return salesOrderRepository.getSalesOrderSummaryMetrics();

        } catch (DataAccessException e) {
            log.error("[ANALYTICS-ORDER] Database error fetching sales summary: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve sales order summary", e);
        } catch (Exception e) {
            log.error("[ANALYTICS-ORDER] Unexpected error:  {}", e.getMessage(), e);
            throw new ServiceException("Failed to retrieve sales order summary", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderSummary getPurchaseOrderSummary() {
        try {
            log.debug("[ANALYTICS-ORDER] Fetching purchase order summary");
            return purchaseOrderRepository.getPurchaseOrderSummaryMetrics();

        } catch (DataAccessException e) {
            log.error("[ANALYTICS-ORDER] Database error fetching purchase summary: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve purchase order summary", e);
        } catch (Exception e) {
            log.error("[ANALYTICS-ORDER] Unexpected error: {}", e.getMessage(), e);
            throw new ServiceException("Failed to retrieve purchase order summary", e);
        }
    }
}
