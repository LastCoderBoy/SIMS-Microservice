package com.sims.simscoreservice.purchaseOrder.queryService;

import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ResourceNotFoundException;
import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.purchaseOrder.dto.PurchaseOrderDetailsView;
import com.sims.simscoreservice.purchaseOrder.dto.SummaryPurchaseOrderView;
import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import com.sims.simscoreservice.purchaseOrder.helper.PurchaseOrderHelper;
import com.sims.simscoreservice.purchaseOrder.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Purchase Order Query Service
 * Centralized read-only operations for purchase orders
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderQueryService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderHelper poHelper;

    /**
     * Find purchase order by ID
     */
    @Transactional(readOnly = true)
    public PurchaseOrder findById(Long orderId) {
        try {
            return purchaseOrderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + orderId));
        } catch (DataAccessException e) {
            log.error("[PO-QUERY] findById() - Database error: {}", e.getMessage());
            throw new DatabaseException("Failed to fetch purchase order", e);
        }
    }

    /**
     * Get all pending purchase orders with pagination
     * Pending = AWAITING_APPROVAL, DELIVERY_IN_PROCESS, PARTIALLY_RECEIVED
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<SummaryPurchaseOrderView> getAllPendingPurchaseOrders(int page, int size,
                                                                                   String sortBy, String sortDirection) {
        try {
            String effectiveSortBy = (sortBy == null || sortBy.trim().isEmpty()) ? "product.name" : sortBy;
            String effectiveSortDirection = (sortDirection == null || sortDirection.trim().isEmpty()) ? "desc" : sortDirection;

            Sort. Direction direction = effectiveSortDirection.equalsIgnoreCase("desc")
                    ? Sort.Direction. DESC
                    : Sort.Direction.ASC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, effectiveSortBy));
            Page<PurchaseOrder> entityPage = purchaseOrderRepository.findAllPendingOrders(pageable);

            PaginatedResponse<SummaryPurchaseOrderView> response = poHelper.toPaginatedSummaryView(entityPage);

            log.info("[PO-QUERY] Returned {} of {} pending orders", response.getContent().size(), response.getTotalElements());

            return response;

        } catch (DataAccessException e) {
            log.error("[PO-QUERY] Get All Pending: Database error : {}", e. getMessage());
            throw new DatabaseException("Failed to fetch pending purchase orders", e);
        } catch (Exception e) {
            log.error("[PO-QUERY] Error fetching pending orders: {}", e.getMessage());
            throw new ServiceException("Failed to fetch pending purchase orders", e);
        }
    }

    /**
     * Get all purchase orders (for OM context)
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<SummaryPurchaseOrderView> getAllPurchaseOrders(int page, int size,
                                                                            String sortBy, String sortDirection) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy). ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<PurchaseOrder> entityResponse = purchaseOrderRepository.findAll(pageable);

            log.info("[PO-QUERY] Returning {} purchase orders", entityResponse.getContent(). size());

            return poHelper.toPaginatedSummaryView(entityResponse);

        } catch (DataAccessException da) {
            log.error("[PO-QUERY] Database error: {}", da.getMessage());
            throw new DatabaseException("Database error", da);
        } catch (PropertyReferenceException e) {
            log.error("[PO-QUERY] Invalid sort field: {}", e.getMessage());
            throw new ValidationException("Invalid sort field: " + e.getMessage());
        } catch (Exception e) {
            log.error("[PO-QUERY] Error fetching orders: {}", e.getMessage());
            throw new ServiceException("Failed to fetch purchase orders", e);
        }
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDetailsView getDetailsForPurchaseOrder(Long orderId) {
        try {
            if (orderId == null || orderId < 1) {
                throw new ValidationException("Invalid order ID: " + orderId);
            }

            PurchaseOrder purchaseOrder = findById(orderId);

            log.info("[PO-QUERY] Returning details for PO ID: {}", orderId);

            return new PurchaseOrderDetailsView(purchaseOrder);

        } catch (DataAccessException da) {
            log.error("[PO-QUERY] Database error while getting Details for PO: {}, {}", orderId, da.getMessage());
            throw new DatabaseException("Database error", da);
        } catch (ResourceNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("[PO-QUERY] Error getting details: {}", e.getMessage());
            throw new ServiceException("Failed to get purchase order details", e);
        }
    }

    /**
     * Get total count of pending purchase orders
     */
    @Transactional(readOnly = true)
    public Long getTotalValidPoSize() {
        try {
            return purchaseOrderRepository.countIncomingPurchaseOrders();
        } catch (DataAccessException e) {
            log.error("[PO-QUERY] Database error counting: {}", e.getMessage());
            throw new DatabaseException("Failed to count purchase orders", e);
        }
    }

    /**
     * Get all overdue purchase orders
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<SummaryPurchaseOrderView> getAllOverduePurchaseOrders(int page, int size) {
        try {
            Sort sort = Sort.by(Sort.Direction.DESC, "expectedArrivalDate");
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<PurchaseOrder> overduePage = purchaseOrderRepository.findAllOverdueOrders(pageable);

            log.info("[PO-QUERY] Found {} overdue orders", overduePage.getTotalElements());

            return poHelper.toPaginatedSummaryView(overduePage);

        } catch (DataAccessException e) {
            log.error("[PO-QUERY] Database error fetching overdue orders: {}", e.getMessage());
            throw new DatabaseException("Failed to fetch overdue orders", e);
        } catch (Exception e) {
            log. error("[PO-QUERY] Error fetching overdue orders: {}", e.getMessage());
            throw new ServiceException("Failed to fetch overdue orders", e);
        }
    }

    /**
     * Save or update purchase order
     */
    @Transactional
    public PurchaseOrder save(PurchaseOrder purchaseOrder) {
        try {
            return purchaseOrderRepository.save(purchaseOrder);
        } catch (Exception e) {
            log.error("[PO-QUERY] Failed to save order: {}", e.getMessage());
            throw new DatabaseException("Failed to save purchase order", e);
        }
    }
}
