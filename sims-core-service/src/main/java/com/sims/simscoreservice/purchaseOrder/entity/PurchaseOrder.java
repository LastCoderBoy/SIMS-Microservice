package com.sims.simscoreservice.purchaseOrder.entity;


import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import com.sims.simscoreservice.supplier.entity.Supplier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Purchase Order Entity
 * Represents orders placed to suppliers for restocking inventory
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Entity
@Table(name = "purchase_orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "po_number", unique = true, nullable = false, length = 50)
    private String poNumber; // Format: PO-{supplierID}-{UUID}

    @Column(name = "ordered_quantity", nullable = false)
    private Integer orderedQuantity;

    @Column(name = "received_quantity", nullable = false)
    private Integer receivedQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PurchaseOrderStatus status;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "expected_arrival_date")
    private LocalDate expectedArrivalDate;

    @Column(name = "actual_arrival_date")
    private LocalDate actualArrivalDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "ordered_by", nullable = false, length = 100)
    private String orderedBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    @Column(name = "version")
    private Integer version; // Optimistic locking


    // ======== Relationships ========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    /**
     * Business constructor for creating new purchase orders
     */
    public PurchaseOrder(Product product,
                         Supplier supplier,
                         Integer orderedQuantity,
                         LocalDate expectedArrivalDate,
                         String notes,
                         String poNumber,
                         LocalDate orderDate,
                         LocalDateTime lastUpdated,
                         String orderedBy) {
        this.product = Objects.requireNonNull(product, "Product cannot be null");
        this.supplier = Objects.requireNonNull(supplier, "Supplier cannot be null");
        this.orderedQuantity = Objects.requireNonNull(orderedQuantity, "Ordered quantity cannot be null");
        this.expectedArrivalDate = expectedArrivalDate;
        this.notes = notes != null ? notes : "";
        this.poNumber = Objects.requireNonNull(poNumber, "PO Number cannot be null");
        this.orderDate = Objects.requireNonNull(orderDate, "Order date cannot be null");
        this.lastUpdated = Objects.requireNonNull(lastUpdated, "Last updated cannot be null");
        this.orderedBy = Objects.requireNonNull(orderedBy, "Ordered by cannot be null");

        // Set defaults
        this.receivedQuantity = 0;
        this.status = PurchaseOrderStatus.AWAITING_APPROVAL;
        this.actualArrivalDate = null;
        this.updatedBy = null;
    }

    /**
     * Convenience constructor with Clock
     */
    public PurchaseOrder(Product product,
                         Supplier supplier,
                         Integer orderedQuantity,
                         LocalDate expectedArrivalDate,
                         String notes,
                         String poNumber,
                         String orderedBy,
                         Clock clock) {
        this(product, supplier, orderedQuantity, expectedArrivalDate, notes, poNumber,
                LocalDate.from(GlobalServiceHelper.now(clock)),
                GlobalServiceHelper.now(clock),
                orderedBy);
    }

    /**
     * Check if order is finalized (cannot be modified)
     */
    public boolean isFinalized() {
        return this.status == PurchaseOrderStatus.RECEIVED ||
                this.status == PurchaseOrderStatus.CANCELLED ||
                this.status == PurchaseOrderStatus.FAILED;
    }
}
