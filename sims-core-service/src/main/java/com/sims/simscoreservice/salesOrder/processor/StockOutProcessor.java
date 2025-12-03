package com.sims.simscoreservice.salesOrder.processor;


import com.sims.simscoreservice.salesOrder.entity.SalesOrder;

import java.util.Map;

/**
 * Stock Out Processor Interface
 * Processes sales order fulfillment (stock out operations)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface StockOutProcessor {

    /**
     * Process stock out for sales order
     *
     * @param salesOrder Sales order to process
     * @param approvedQuantities Map of productId -> approved quantity
     * @param username User processing the order
     * @return Updated sales order
     */
    SalesOrder processStockOut(SalesOrder salesOrder, Map<String, Integer> approvedQuantities, String username);
}
