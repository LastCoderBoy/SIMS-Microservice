package com.sims.simscoreservice.orderManagement.salesOrder.service.queryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Shared query service for Sales Order read operations
 * Purpose: Break circular dependencies between SalesOrderService and other services
 * Contains ONLY read operations - no business logic or state changes
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderQueryService {

    /**
     * Count active orders for product
     */
    @Transactional(readOnly = true)
    public long countActiveOrdersForProduct(String productId) {
        return null;
//        return salesOrderRepository.countActiveOrdersForProduct(productId);
    }
}
