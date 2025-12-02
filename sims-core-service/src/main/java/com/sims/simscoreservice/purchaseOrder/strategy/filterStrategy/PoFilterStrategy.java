package com.sims.simscoreservice.purchaseOrder.strategy.filterStrategy;

import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.purchaseOrder.dto.SummaryPurchaseOrderView;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;
import org.springframework.data.domain.Pageable;

/**
 * Purchase Order Filter Strategy
 * Strategy pattern for different filter contexts
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface PoFilterStrategy {

    /**
     * Filter purchase orders
     *
     * @param category product category
     * @param status purchase order status
     * @param pageable pagination info
     * @return paginated filtered results
     */
    PaginatedResponse<SummaryPurchaseOrderView> filterPurchaseOrders(
            ProductCategories category,
            PurchaseOrderStatus status,
            Pageable pageable
    );
}
