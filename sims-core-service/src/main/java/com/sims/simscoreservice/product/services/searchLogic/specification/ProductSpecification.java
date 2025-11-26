package com.sims.simscoreservice.product.services.searchLogic.specification;

import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.product.enums.ProductStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Product Specification
 * Dynamic query builder for filtering products
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public class ProductSpecification {

    public static Specification<Product> hasCategory(ProductCategories category) {
        return (root, query, criteriaBuilder) -> {
            if (category == null) return null;
            return criteriaBuilder.equal(root.get("category"), category);
        };
    }

    public static Specification<Product> hasLocation(String location) {
        return (root, query, criteriaBuilder) -> {
            if (location == null || location.trim().isEmpty()) return null;
            return criteriaBuilder.equal(root.get("location"), location);
        };
    }

    public static Specification<Product> hasPriceLessThanOrEqual(BigDecimal price) {
        return (root, query, criteriaBuilder) -> {
            if (price == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get("price"), price);
        };
    }

    public static Specification<Product> hasStatus(ProductStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return null;
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }


    // For the general filter (location, category, or status)
    public static Specification<Product> generalFilter(String filter) {
        return (root, query, criteriaBuilder) -> {
            if (filter == null || filter.trim().isEmpty()) return null;

            String searchPattern = "%" + filter.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("category").as(String.class)), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("status").as(String.class)), searchPattern)
            );
        };
    }
}
