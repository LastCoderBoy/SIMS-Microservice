package com.sims.simscoreservice.purchaseOrder.strategy.filterStrategy;


import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import com.sims.simscoreservice.purchaseOrder.helper.PurchaseOrderHelper;
import com.sims.simscoreservice.purchaseOrder.repository.PurchaseOrderRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Filter All Purchase Orders
 * Filters all orders (OM context)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component("filterAllPurchaseOrders")
public class FilterAllPurchaseOrders extends AbstractPoFilterStrategy {

    public FilterAllPurchaseOrders(PurchaseOrderHelper poHelper,
                                   PurchaseOrderRepository purchaseOrderRepository) {
        super(poHelper, purchaseOrderRepository);
    }

    @Override
    @Nullable
    protected Specification<PurchaseOrder> baseSpecType() {
        // No base filter - return all orders
        return null;
    }
}
