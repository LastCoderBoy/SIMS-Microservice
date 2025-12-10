package com.sims.simscoreservice.salesOrder.entity;

import com.sims.simscoreservice.qrCode.entity.SalesOrderQRCode;
import com.sims.simscoreservice.salesOrder.dto.SalesOrderRequest;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sales Order Entity
 * Represents customer orders for products
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Entity
@Table(name = "sales_orders")
@Data
@ToString(exclude = {"items", "qrCode"})
@EqualsAndHashCode(exclude = {"items", "qrCode"})
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_reference", nullable = false, unique = true, length = 30)
    private String orderReference; // Format: SO-2024-07-20-001

    @Column(name = "destination", nullable = false, length = 255)
    private String destination;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "confirmed_by", length = 100)
    private String confirmedBy;     // Person who is confirming the SalesOrder in the IC.

    @Column(name = "cancelled_by", length = 100)
    private String cancelledBy;     // Person who is cancelling the SalesOrder in the IC.

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SalesOrderStatus status;     // PENDING, PARTIALLY_APPROVED, PARTIALLY_DELIVERED, APPROVED, DELIVERED, COMPLETED, CANCELLED

    @CreationTimestamp
    @Column(name = "order_date", nullable = false, updatable = false)
    private LocalDateTime orderDate;

    @Column(name = "estimated_delivery_date", nullable = false)
    private LocalDateTime estimatedDeliveryDate;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdate;

    // Relationships
    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "qr_code_id", unique = true)
    private SalesOrderQRCode qrCode;

    /**
     * Constructor for creating new sales order
     */
    public SalesOrder(String orderReference, String destination, SalesOrderStatus status, List<OrderItem> items) {
        this.orderReference = orderReference;
        this.destination = destination;
        this.status = status;
        if (items != null) {
            for (OrderItem item : items) {
                this.addOrderItem(item);
            }
        }
    }

    /**
     * Constructor for creating sales order from request
     */
    public SalesOrder(SalesOrderRequest request, String orderReference, String createdBy, SalesOrderQRCode qrCode) {
        this.orderReference = orderReference;
        this.destination = request.getDestination().trim();
        this.customerName = request.getCustomerName().trim();
        this.status = SalesOrderStatus.PENDING;
        this.createdBy = createdBy;
        this.estimatedDeliveryDate = LocalDateTime.now().plusDays(7);
        this.setQrCode(qrCode);
        this.items = new ArrayList<>();
    }

    /**
     * Add order item with bidirectional relationship
     */
    public void addOrderItem(OrderItem item) {
        items.add(item);
        item.setSalesOrder(this);
    }

    /**
     * Remove order item
     */
    public void removeOrderItem(OrderItem item) {
        items.remove(item);
        item.setSalesOrder(null);
    }

    /**
     * Set QR code with bidirectional relationship
     */
    public void setQrCode(SalesOrderQRCode qrCode) {
        this.qrCode = qrCode;
        qrCode.setSalesOrder(this);
    }

    /**
     * Check if order is finalized (cannot be modified)
     */
    public boolean isFinalized() {
        return this.status == SalesOrderStatus.CANCELLED ||
                this.status == SalesOrderStatus.DELIVERED ||
                this.status == SalesOrderStatus.APPROVED;
    }
}