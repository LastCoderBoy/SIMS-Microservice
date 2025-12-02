package com.sims.simscoreservice.purchaseOrder.strategy;

import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.purchaseOrder.dto.SummaryPurchaseOrderView;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;

public interface PurchaseOrderSearchService {
    PaginatedResponse<SummaryPurchaseOrderView> searchPending(String text, int page, int size,
                                                              String sortBy, String sortDirection);

    PaginatedResponse<SummaryPurchaseOrderView> filterPending(PurchaseOrderStatus status, ProductCategories category,
                                                              String sortBy, String sortDirection, int page, int size);

    PaginatedResponse<SummaryPurchaseOrderView> searchAll(String text, int page, int size, String sortBy, String sortDirection);

    PaginatedResponse<SummaryPurchaseOrderView> filterAll(ProductCategories category, PurchaseOrderStatus status,
                                                          String sortBy, String sortDirection, int page, int size);
}
