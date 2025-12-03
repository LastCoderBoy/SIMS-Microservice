package com.sims.simscoreservice.salesOrder.entity;

import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.salesOrder.enums.OrderItemStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Order Item Entity
 * Represents individual products within a sales order
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Entity
@Table(name = "order_items")
@Data
@ToString(exclude = "salesOrder")
@EqualsAndHashCode(exclude = "salesOrder")
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "approved_quantity", nullable = false)
    private Integer approvedQuantity = 0;

    @Column(name = "order_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal orderPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderItemStatus status = OrderItemStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    /**
     * Constructor for creating new order item
     */
    public OrderItem(Integer quantity, Product product, BigDecimal orderPrice) {
        this.quantity = quantity;
        this.product = product;
        this.orderPrice = orderPrice;
        this.status = OrderItemStatus.PENDING;
        this.approvedQuantity = 0;
    }

    /**
     * Check if item is finalized (cannot be modified)
     */
    public boolean isFinalized() {
        return this.status == OrderItemStatus.APPROVED ||
                this.status == OrderItemStatus.CANCELLED;
    }
}
