package com.sims.simscoreservice.salesOrder.strategy.filterStrategy;

import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Sales Order Filter Strategy
 * Strategy pattern for different filter contexts
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface SoFilterStrategy {

    /**
     * Filter sales orders
     *
     * @param status sales order status
     * @param optionDate date field to filter by (orderDate, deliveryDate, estimatedDeliveryDate)
     * @param startDate start date for range
     * @param endDate end date for range
     * @param pageable pagination info
     * @return Page of filtered sales orders
     */
    Page<SalesOrder> filterSalesOrders(SalesOrderStatus status, String optionDate,
                                       LocalDate startDate, LocalDate endDate, Pageable pageable);
}
