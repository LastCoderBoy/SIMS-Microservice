package com.sims.simscoreservice.inventory.service;

import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.enums.InventoryStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inventory Status Service
 * Updates inventory status based on stock levels
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryStatusService {

    // TODO: Inject when implementing alerts
    // private final LowStockAlertService lowStockAlertService;

    /**
     * Update inventory status based on current stock levels
     *
     * @param inventory Inventory to update
     */
    @Transactional
    public void updateInventoryStatus(Inventory inventory) {
        if (inventory.getStatus() != InventoryStatus. INVALID) {
            if (inventory.getCurrentStock() <= inventory.getMinLevel()) {
                inventory.setStatus(InventoryStatus.LOW_STOCK);

                // TODO: Trigger low stock alert
                // lowStockAlertService.sendLowStockAlert(inventory);

                log.warn("[INVENTORY-STATUS] Product {} is now LOW_STOCK.  Current: {}, Min: {}",
                        inventory.getProduct().getProductId(),
                        inventory.getCurrentStock(),
                        inventory.getMinLevel());
            } else {
                inventory.setStatus(InventoryStatus.IN_STOCK);
                log.debug("[INVENTORY-STATUS] Product {} is IN_STOCK",
                        inventory.getProduct().getProductId());
            }
        }
    }
}