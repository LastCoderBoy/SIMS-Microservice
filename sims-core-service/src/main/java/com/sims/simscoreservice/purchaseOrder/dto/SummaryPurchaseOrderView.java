package com.sims.simscoreservice.purchaseOrder.dto;


import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Summary Purchase Order View DTO
 * Used for list views and tables
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryPurchaseOrderView {
    private Long id;
    private String poNumber;
    private PurchaseOrderStatus status;
    private LocalDate orderDate;
    private LocalDate expectedArrivalDate;
    private LocalDate actualArrivalDate;
    private Integer orderedQuantity;
    private Integer receivedQuantity;
    private String productName;
    private ProductCategories productCategory;
    private String supplierName;

    /**
     * Constructor from entity
     */
    public SummaryPurchaseOrderView(PurchaseOrder order) {
        this.id = order.getId();
        this.poNumber = order.getPoNumber();
        this.status = order.getStatus();
        this.orderDate = order.getOrderDate();
        this.expectedArrivalDate = order.getExpectedArrivalDate();
        this.actualArrivalDate = order.getActualArrivalDate();
        this.orderedQuantity = order.getOrderedQuantity();
        this.receivedQuantity = order.getReceivedQuantity();
        this.productName = order.getProduct() != null ? order.getProduct().getName() : "N/A";
        this.productCategory = order.getProduct() != null && order.getProduct().getCategory() != null
                ? order.getProduct().getCategory()
                : null;
        this.supplierName = order.getSupplier() != null ? order.getSupplier().getName() : "N/A";
    }
}
