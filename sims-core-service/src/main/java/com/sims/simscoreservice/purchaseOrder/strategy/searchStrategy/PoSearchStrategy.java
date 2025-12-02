package com.sims.simscoreservice.purchaseOrder.strategy.searchStrategy;


import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import org.springframework.data.domain.Page;

/**
 * Purchase Order Search Strategy
 * Strategy pattern for different search contexts (IC vs OM)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface PoSearchStrategy {

    /**
     * Search purchase orders
     *
     * @param text search text
     * @param page page number
     * @param size page size
     * @param sortBy field to sort by
     * @param sortDirection asc or desc
     * @return Page of PurchaseOrder
     */
    Page<PurchaseOrder> searchInPos(String text, int page, int size, String sortBy, String sortDirection);
}
