package com.sims.simscoreservice.purchaseOrder.strategy.filterStrategy;


import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import com.sims.simscoreservice.purchaseOrder.helper.PurchaseOrderHelper;
import com.sims.simscoreservice.purchaseOrder.repository.PurchaseOrderRepository;
import com.sims.simscoreservice.purchaseOrder.specification.PurchaseOrderSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Filter Pending Purchase Orders
 * Filters only pending orders (IC context)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component("filterPendingPurchaseOrders")
public class FilterPendingPurchaseOrders extends AbstractPoFilterStrategy {

    public FilterPendingPurchaseOrders(PurchaseOrderHelper poHelper,
                                       PurchaseOrderRepository purchaseOrderRepository) {
        super(poHelper, purchaseOrderRepository);
    }

    @Override
    @Nullable
    protected Specification<PurchaseOrder> baseSpecType() {
        // Filter only pending orders
        return PurchaseOrderSpecification.isPending();
    }
}
