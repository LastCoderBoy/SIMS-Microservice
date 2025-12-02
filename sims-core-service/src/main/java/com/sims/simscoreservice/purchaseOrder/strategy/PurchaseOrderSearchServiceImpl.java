package com.sims.simscoreservice.purchaseOrder.strategy;


import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.purchaseOrder.dto.SummaryPurchaseOrderView;
import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;
import com.sims.simscoreservice.purchaseOrder.helper.PurchaseOrderHelper;
import com.sims.simscoreservice.purchaseOrder.queryService.PurchaseOrderQueryService;
import com.sims.simscoreservice.purchaseOrder.strategy.filterStrategy.PoFilterStrategy;
import com.sims.simscoreservice.purchaseOrder.strategy.searchStrategy.PoSearchStrategy;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Purchase Order Search Service Implementation
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderSearchServiceImpl implements PurchaseOrderSearchService {

    private final GlobalServiceHelper globalServiceHelper;
    private final PurchaseOrderHelper poHelper;
    private final PurchaseOrderQueryService queryService;

    // Search strategies
    private final PoSearchStrategy icPoSearchStrategy;
    private final PoSearchStrategy omPoSearchStrategy;

    // Filter strategies
    private final PoFilterStrategy filterPendingPurchaseOrders;
    private final PoFilterStrategy filterAllPurchaseOrders;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummaryPurchaseOrderView> searchPending(String text, int page, int size,
                                                                     String sortBy, String sortDirection) {
        globalServiceHelper.validatePaginationParameters(page, size);

        // If no search text, return all pending orders
        if (text == null || text. trim().isEmpty()) {
            log.info("[PO-SEARCH] No search text, returning all pending orders");
            return queryService.getAllPendingPurchaseOrders(page, size, sortBy, sortDirection);
        }

        // Search pending orders
        Page<PurchaseOrder> result = icPoSearchStrategy.searchInPos(text, page, size, sortBy, sortDirection);
        return poHelper.toPaginatedSummaryView(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummaryPurchaseOrderView> filterPending(PurchaseOrderStatus status,
                                                                     ProductCategories category,
                                                                     String sortBy, String sortDirection,
                                                                     int page, int size) {
        Sort. Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest. of(page, size, Sort. by(direction, sortBy));

        return filterPendingPurchaseOrders.filterPurchaseOrders(category, status, pageable);
    }

    // OM Context methods (for Order Management module - implement later)
    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummaryPurchaseOrderView> searchAll(String text, int page, int size,
                                                                 String sortBy, String sortDirection) {
        globalServiceHelper. validatePaginationParameters(page, size);

        if (text == null || text.trim().isEmpty()) {
            return queryService.getAllPurchaseOrders(page, size, sortBy, sortDirection);
        }

        Page<PurchaseOrder> result = omPoSearchStrategy.searchInPos(text, page, size, sortBy, sortDirection);
        return poHelper.toPaginatedSummaryView(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummaryPurchaseOrderView> filterAll(ProductCategories category,
                                                                 PurchaseOrderStatus status,
                                                                 String sortBy, String sortDirection,
                                                                 int page, int size) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return filterAllPurchaseOrders.filterPurchaseOrders(category, status, pageable);
    }
}
