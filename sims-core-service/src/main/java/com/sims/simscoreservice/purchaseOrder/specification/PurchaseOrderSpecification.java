package com.sims.simscoreservice.purchaseOrder.specification;


import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

/**
 * Purchase Order Specification
 * Dynamic query builder for filtering purchase orders
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public class PurchaseOrderSpecification {

    /**
     * Filter pending purchase orders
     * Pending = AWAITING_APPROVAL, DELIVERY_IN_PROCESS, PARTIALLY_RECEIVED
     */
    public static Specification<PurchaseOrder> isPending() {
        return (root, query, cb) -> root.get("status"). in(
                PurchaseOrderStatus.AWAITING_APPROVAL,
                PurchaseOrderStatus.DELIVERY_IN_PROCESS,
                PurchaseOrderStatus.PARTIALLY_RECEIVED
        );
    }

    /**
     * Filter by status
     */
    public static Specification<PurchaseOrder> hasStatus(PurchaseOrderStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return null;
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Filter by product category
     */
    public static Specification<PurchaseOrder> hasProductCategory(ProductCategories category) {
        return (root, query, criteriaBuilder) -> {
            if (category == null) return null;

            // Join with product entity to access category
            Join<PurchaseOrder, Product> productJoin = root.join("product");
            return criteriaBuilder.equal(productJoin.get("category"), category);
        };
    }
}
