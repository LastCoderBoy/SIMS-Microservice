package com.sims.simscoreservice.inventory.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Pending Order Response
 * Unified DTO for both Sales Orders and Purchase Orders
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingOrderResponse {

    private Long id;
    private String orderReference;  // PO Number or SO Order Reference
    private String type;  // "SALES_ORDER" or "PURCHASE_ORDER"
    private String status;
    private LocalDateTime orderDate;
    private LocalDateTime estimatedDate;  // Delivery date (SO) or Arrival date (PO)
    private String customerOrSupplierName;
    private Integer totalOrderedQuantity;
}
