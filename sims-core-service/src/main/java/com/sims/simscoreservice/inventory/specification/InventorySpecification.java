package com.sims.simscoreservice.inventory.specification;

import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.enums.InventoryStatus;
import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.product.enums.ProductCategories;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa. domain.Specification;

/**
 * Inventory Specification
 * Dynamic query builder for filtering inventory
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public class InventorySpecification {

    /**
     * Filter by inventory status
     */
    public static Specification<Inventory> hasStatus(InventoryStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return null;
            return criteriaBuilder.equal(root. get("status"), status);
        };
    }

    /**
     * Filter by product category
     */
    public static Specification<Inventory> hasProductCategory(ProductCategories category) {
        return (root, query, criteriaBuilder) -> {
            if (category == null) return null;
            Join<Inventory, Product> joinProduct = root.join("product");
            return criteriaBuilder.equal(joinProduct.get("category"), category);
        };
    }

    /**
     * Filter low stock items (current <= min level, status != INVALID)
     */
    public static Specification<Inventory> hasLowStock() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.notEqual(root.get("status"), InventoryStatus.INVALID),
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("currentStock"),
                        root.get("minLevel")
                )
        );
    }

    /**
     * Filter by stock level
     */
    public static Specification<Inventory> hasStockLessThanOrEqual(Integer level) {
        return (root, query, criteriaBuilder) -> {
            if (level == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get("currentStock"), level);
        };
    }

    /**
     * Exclude invalid inventory
     */
    public static Specification<Inventory> excludeInvalid() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.notEqual(root.get("status"), InventoryStatus.INVALID);
    }
}