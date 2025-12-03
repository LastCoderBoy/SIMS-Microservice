package com.sims.simscoreservice.salesOrder.dto;


import com.sims.simscoreservice.salesOrder.dto.orderItem.OrderItemResponse;
import com.sims.simscoreservice.salesOrder.entity.OrderItem;
import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * Detailed Sales Order View DTO
 * Used for detail pages with full information
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailedSalesOrderView {
    // Existing fields from SalesOrderView
    private Long Id;
    private String orderReference;
    private String destination;
    private SalesOrderStatus status;
    private LocalDateTime orderDate;
    private LocalDateTime estimatedDeliveryDate;
    private String customerName;
    private Integer totalItems; // Sum of quantities
    private BigDecimal totalAmount; // Sum of orderPrice * quantity
    private Integer totalApprovedQuantity;

    // New fields
    private LocalDateTime deliveryDate;
    private List<OrderItemResponse> items; // Nested DTO for OrderItem
    private String confirmedBy;
    private LocalDateTime lastUpdate;

    public DetailedSalesOrderView(SalesOrder salesOrder) {
        this.Id = salesOrder.getId();
        this.orderReference = salesOrder.getOrderReference();
        this.destination = salesOrder.getDestination();
        this.status = salesOrder.getStatus();
        this.orderDate = salesOrder.getOrderDate();
        this.estimatedDeliveryDate = salesOrder.getEstimatedDeliveryDate();
        this.customerName = salesOrder.getCustomerName();
        this.totalItems = salesOrder.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
        this.totalAmount = salesOrder.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalApprovedQuantity = salesOrder.getItems().stream().mapToInt(OrderItem::getApprovedQuantity).sum();

        // Set up New fields
        this.deliveryDate = salesOrder.getDeliveryDate();
        this.items = salesOrder.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getProductId(),
                        item.getProduct().getName(),
                        item.getProduct().getCategory(),
                        item.getStatus(),
                        item.getQuantity(),
                        item.getApprovedQuantity(),
                        item.getProduct().getPrice(),
                        item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                )).toList();
        this.confirmedBy = salesOrder.getConfirmedBy();
        this.lastUpdate = salesOrder.getLastUpdate();
    }
}

