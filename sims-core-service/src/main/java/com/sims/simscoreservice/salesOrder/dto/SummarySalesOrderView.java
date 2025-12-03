package com.sims.simscoreservice.salesOrder.dto;


import com.sims.simscoreservice.salesOrder.entity.OrderItem;
import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Summary Sales Order View DTO
 * Used for list views and tables
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SummarySalesOrderView {
    private Long Id;
    private String orderReference;
    private String destination;
    private SalesOrderStatus status;
    private LocalDateTime orderDate;
    private LocalDateTime estimatedDeliveryDate;
    private String customerName;
    private Integer totalOrderedQuantity; // Sum of ordered quantities
    private BigDecimal totalAmount; // Sum of orderPrice * quantity
    private Integer totalApprovedQuantity;

    public SummarySalesOrderView(SalesOrder salesOrder){
        this.Id = salesOrder.getId();
        this.orderReference = salesOrder.getOrderReference();
        this.destination = salesOrder.getDestination();
        this.status = salesOrder.getStatus();
        this.orderDate = salesOrder.getOrderDate();
        this.estimatedDeliveryDate = salesOrder.getEstimatedDeliveryDate();
        this.customerName = salesOrder.getCustomerName();
        this.totalOrderedQuantity = salesOrder.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
        this.totalAmount = salesOrder.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalApprovedQuantity = salesOrder.getItems().stream().mapToInt(OrderItem::getApprovedQuantity).sum();
    }
}
