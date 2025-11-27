package com.sims.simscoreservice.inventory.dto;

import com.sims.common.models.PaginatedResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok. NoArgsConstructor;

/**
 * Inventory Dashboard Page Response
 * Contains all metrics and pending orders
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryPageResponse {

    private Long totalInventorySize;
    private Long lowStockSize;
    private Long incomingStockSize;  // Pending Purchase Orders
    private Long outgoingStockSize;  // Pending Sales Orders
    private Long damageLossSize;

    private PaginatedResponse<PendingOrderResponse> allPendingOrders;
}
