package com.sims.simscoreservice.salesOrder.strategy.filterStrategy;

import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.repository.SalesOrderRepository;
import org.springframework.data.jpa. domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype. Component;

/**
 * Filter Sales Orders in OM
 * Filters all orders (OM context)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component("filterSalesOrdersInOm")
public class FilterSalesOrdersInOm extends AbstractSoFilterStrategy {

    public FilterSalesOrdersInOm(SalesOrderRepository salesOrderRepository) {
        super(salesOrderRepository);
    }

    @Override
    @Nullable
    protected Specification<SalesOrder> baseSpecType() {
        // No base filter - return all orders
        return null;
    }
}
