package com.sims.simscoreservice.salesOrder.specification;

import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Sales Order Specification
 * Dynamic query builder for filtering sales orders
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public class SalesOrderSpecification {

    /**
     * Filter by pending/waiting status
     * Waiting = PENDING, PARTIALLY_DELIVERED, PARTIALLY_APPROVED
     */
    public static Specification<SalesOrder> byWaitingStatus() {
        return (root, query, criteriaBuilder) ->
                root.get("status").in(
                        SalesOrderStatus.PENDING,
                        SalesOrderStatus.PARTIALLY_DELIVERED,
                        SalesOrderStatus.PARTIALLY_APPROVED
                );
    }

    /**
     * Filter by specific status
     */
    public static Specification<SalesOrder> byStatus(SalesOrderStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return null;
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Filter by date range
     *
     * @param optionDate date field to filter (orderDate, deliveryDate, estimatedDeliveryDate)
     * @param startDate start date
     * @param endDate end date
     */
    public static Specification<SalesOrder> byDatesBetween(String optionDate, LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (optionDate == null || optionDate.isEmpty()) return null;

            return switch (optionDate.toLowerCase()) {
                case "orderdate" -> criteriaBuilder.between(root.get("orderDate"), startDate, endDate);
                case "deliverydate" -> criteriaBuilder.between(root.get("deliveryDate"), startDate, endDate);
                case "estimateddeliverydate" -> criteriaBuilder.between(root.get("estimatedDeliveryDate"), startDate, endDate);
                default -> null;
            };
        };
    }
}
