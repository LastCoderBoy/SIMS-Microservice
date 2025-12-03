package com.sims.simscoreservice.salesOrder.strategy.searchStrategy;

import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import org.springframework.data.domain.Page;

/**
 * Sales Order Search Strategy
 * Strategy pattern for different search contexts (IC vs OM)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface SoSearchStrategy {

    /**
     * Search sales orders
     *
     * @param text search text
     * @param page page number
     * @param size page size
     * @param sortBy field to sort by
     * @param sortDirection asc or desc
     * @return Page of SalesOrder
     */
    Page<SalesOrder> searchInSo(String text, int page, int size, String sortBy, String sortDirection);
}
