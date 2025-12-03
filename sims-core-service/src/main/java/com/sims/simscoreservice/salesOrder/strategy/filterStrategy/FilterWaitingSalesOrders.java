package com.sims.simscoreservice.salesOrder.strategy.filterStrategy;

import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.repository.SalesOrderRepository;
import com.sims.simscoreservice.salesOrder.specification.SalesOrderSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Filter Waiting Sales Orders
 * Filters only pending/waiting orders (IC context)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component("filterWaitingSalesOrders")
public class FilterWaitingSalesOrders extends AbstractSoFilterStrategy {

    public FilterWaitingSalesOrders(SalesOrderRepository salesOrderRepository) {
        super(salesOrderRepository);
    }

    @Override
    @Nullable
    protected Specification<SalesOrder> baseSpecType() {
        // Filter only waiting/pending orders
        return SalesOrderSpecification.byWaitingStatus();
    }
}
