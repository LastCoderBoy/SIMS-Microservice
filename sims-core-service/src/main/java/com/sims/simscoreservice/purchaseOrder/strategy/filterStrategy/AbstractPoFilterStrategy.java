package com.sims.simscoreservice.purchaseOrder.strategy.filterStrategy;


import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.purchaseOrder.dto.SummaryPurchaseOrderView;
import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;
import com.sims.simscoreservice.purchaseOrder.helper.PurchaseOrderHelper;
import com.sims.simscoreservice.purchaseOrder.repository.PurchaseOrderRepository;
import com.sims.simscoreservice.purchaseOrder.specification.PurchaseOrderSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abstract Purchase Order Filter Strategy
 * Base class for filter strategies with common logic
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractPoFilterStrategy implements PoFilterStrategy {

    protected final PurchaseOrderHelper poHelper;
    protected final PurchaseOrderRepository purchaseOrderRepository;

    /**
     * Defines the base specification for the filter strategy
     * (e.g. pending only or all orders)
     *
     * @return Specification or null if no base filter
     */
    @Nullable
    protected abstract Specification<PurchaseOrder> baseSpecType();

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummaryPurchaseOrderView> filterPurchaseOrders(
            ProductCategories category,
            PurchaseOrderStatus status,
            Pageable pageable) {
        try {
            // Start with base specification
            Specification<PurchaseOrder> spec = Specification.where(baseSpecType());

            // Add status filter if provided
            if (status != null) {
                spec = spec.and(PurchaseOrderSpecification.hasStatus(status));
            }

            // Add category filter if provided
            if (category != null) {
                spec = spec.and(PurchaseOrderSpecification.hasProductCategory(category));
            }

            // Execute query
            Page<PurchaseOrder> filterResult = purchaseOrderRepository.findAll(spec, pageable);

            log.info("[PO-FILTER] Filtered {} results", filterResult.getTotalElements());

            return poHelper.toPaginatedSummaryView(filterResult);

        } catch (IllegalArgumentException iae) {
            log.error("[PO-FILTER] Invalid parameters: {}", iae.getMessage());
            throw new ValidationException("Invalid filter parameters: " + iae.getMessage());
        } catch (Exception e) {
            log.error("[PO-FILTER] Error filtering: {}", e.getMessage());
            throw new ServiceException("Failed to filter orders", e);
        }
    }
}
