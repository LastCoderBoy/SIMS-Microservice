package com.sims.simscoreservice.salesOrder.queryService;

import com.sims.common.exceptions.*;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.salesOrder.dto.DetailedSalesOrderView;
import com.sims.simscoreservice.salesOrder.dto.SummarySalesOrderView;
import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.helper.SalesOrderHelper;
import com.sims.simscoreservice.salesOrder.repository.SalesOrderRepository;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Sales Order Query Service
 * Centralized read-only operations for sales orders
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SalesOrderQueryService {

    private static final LocalDateTime URGENT_DELIVERY_DATE = LocalDateTime.now().plusDays(2);

    private final GlobalServiceHelper globalServiceHelper;
    private final SalesOrderHelper salesOrderHelper;
    private final SalesOrderRepository salesOrderRepository;

    @Transactional(readOnly = true)
    public SalesOrder findById(Long orderId) {
        return salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with ID: " + orderId));
    }

    /**
     * Count active orders for product
     */
    @Transactional(readOnly = true)
    public long countActiveOrdersForProduct(String productId) {
        return salesOrderRepository.countActiveOrdersForProduct(productId);
    }

    /**
     * Get all outgoing (pending) sales orders
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<SummarySalesOrderView> getAllOutgoingSalesOrders(int page, int size, String sortBy, String sortDir) {
        try {
            Pageable pageable = globalServiceHelper.preparePageable(page, size, sortBy, sortDir);
            Page<SalesOrder> salesOrders = salesOrderRepository.findAllOutgoingSalesOrders(pageable);

            log.info("[SO-QUERY] Returning {} outgoing orders", salesOrders.getContent().size());

            return salesOrderHelper.toPaginatedSummaryView(salesOrders);

        } catch (Exception e) {
            log.error("[SO-QUERY] Error fetching outgoing orders: {}", e.getMessage());
            throw new ServiceException("Failed to fetch outgoing orders", e);
        }
    }

    /**
     * Get all urgent sales orders (delivery < 2 days)
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<SummarySalesOrderView> getAllUrgentSalesOrders(int page, int size, String sortBy, String sortDir) {
        try {
            Pageable pageable = globalServiceHelper.preparePageable(page, size, sortBy, sortDir);
            Page<SalesOrder> urgentOrders = salesOrderRepository.findAllUrgentSalesOrders(pageable, URGENT_DELIVERY_DATE);

            log.info("[SO-QUERY] Returning {} urgent orders", urgentOrders.getTotalElements());

            return salesOrderHelper.toPaginatedSummaryView(urgentOrders);

        } catch (DataAccessException da) {
            log.error("[SO-QUERY] Database error fetching urgent orders: {}", da.getMessage());
            throw new DatabaseException("Failed to retrieve urgent orders", da);
        } catch (Exception e) {
            log.error("[SO-QUERY] Error fetching urgent orders: {}", e.getMessage());
            throw new ServiceException("Failed to fetch urgent orders", e);
        }
    }

    /**
     * Get all sales orders (for OM context)
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<SummarySalesOrderView> getAllSummarySalesOrders(String sortBy, String sortDirection, int page, int size) {
        try {
            Pageable pageable = globalServiceHelper.preparePageable(page, size, sortBy, sortDirection);
            Page<SalesOrder> salesOrderPage = salesOrderRepository.findAll(pageable);

            log.info("[SO-QUERY] Returning {} sales orders", salesOrderPage.getContent().size());

            return salesOrderHelper.toPaginatedSummaryView(salesOrderPage);

        } catch (DataAccessException da) {
            log.error("[SO-QUERY] Get All Summary Database error: {}", da.getMessage());
            throw new DatabaseException("Database error occurred", da);
        } catch (PropertyReferenceException e) {
            log.error("[SO-QUERY] Invalid sort field: {}", e.getMessage());
            throw new ValidationException("Invalid sort field provided: " + e.getMessage());
        } catch (Exception e) {
            log.error("[SO-QUERY] Error fetching orders: {}", e.getMessage());
            throw new ServiceException("Failed to fetch sales orders", e);
        }
    }

    /**
     * Get sales order details
     */
    @Transactional(readOnly = true)
    public DetailedSalesOrderView getDetailsForSalesOrder(Long orderId) {
        try {
            if (orderId == null || orderId < 1) {
                throw new ValidationException("Invalid order ID: " + orderId);
            }

            SalesOrder salesOrder = findById(orderId);

            log.info("[SO-QUERY] Returning detailed view for order ID: {}", orderId);

            return new DetailedSalesOrderView(salesOrder);

        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (DataAccessException da) {
            log.error("[SO-QUERY] Database error: {}", da.getMessage());
            throw new DatabaseException("Database error occurred", da);
        } catch (Exception e) {
            log.error("[SO-QUERY] Error getting details: {}", e.getMessage());
            throw new ServiceException("Failed to get order details", e);
        }
    }

    /**
     * Count outgoing sales orders
     */
    @Transactional(readOnly = true)
    public Long countOutgoingSalesOrders() {
        try {
            return salesOrderRepository.countOutgoingSalesOrders();
        } catch (Exception e) {
            log.error("[SO-QUERY] Error counting outgoing orders: {}", e.getMessage());
            throw new ServiceException("Failed to count outgoing orders", e);
        }
    }

    /**
     * Count in-progress sales orders
     */
    @Transactional(readOnly = true)
    public Long countInProgressSalesOrders() {
        try {
            return salesOrderRepository.countInProgressSalesOrders();
        } catch (DataAccessException e) {
            log.error("[SO-QUERY] Database error counting orders: {}", e.getMessage());
            throw new DatabaseException("Failed to count sales orders", e);
        }
    }
}
