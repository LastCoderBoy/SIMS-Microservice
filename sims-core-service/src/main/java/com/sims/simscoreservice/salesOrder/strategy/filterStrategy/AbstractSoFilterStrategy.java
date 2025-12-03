package com.sims.simscoreservice.salesOrder.strategy.filterStrategy;

import com.sims.common.exceptions.ServiceException;
import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import com.sims.simscoreservice.salesOrder.repository.SalesOrderRepository;
import com.sims.simscoreservice.salesOrder.specification.SalesOrderSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Abstract Sales Order Filter Strategy
 * Base class for filter strategies with common logic
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractSoFilterStrategy implements SoFilterStrategy {

    protected final SalesOrderRepository salesOrderRepository;

    /**
     * Defines the base specification for the filter strategy
     * (e.g., pending only or all orders)
     *
     * @return Specification or null if no base filter
     */
    @Nullable
    protected abstract Specification<SalesOrder> baseSpecType();

    @Override
    @Transactional(readOnly = true)
    public Page<SalesOrder> filterSalesOrders(SalesOrderStatus status, String optionDate,
                                              LocalDate startDate, LocalDate endDate, Pageable pageable) {
        try {
            // Start with base specification
            Specification<SalesOrder> specification = Specification.where(baseSpecType());

            // Add status filter if provided
            if (status != null) {
                specification = specification.and(SalesOrderSpecification.byStatus(status));
            }

            // Add date range filter if provided
            if (optionDate != null && !optionDate. isEmpty()) {
                validateDateRange(startDate, endDate);
                specification = specification.and(
                        SalesOrderSpecification.byDatesBetween(optionDate, startDate, endDate)
                );
            }

            // Execute query
            return salesOrderRepository.findAll(specification, pageable);

        } catch (IllegalArgumentException ie) {
            log.error("[SO-FILTER] Invalid parameters: {}", ie.getMessage());
            throw ie;
        } catch (Exception e) {
            log.error("[SO-FILTER] Error filtering orders: {}", e.getMessage());
            throw new ServiceException("Failed to filter orders", e);
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date must be provided for date filtering");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }
}
