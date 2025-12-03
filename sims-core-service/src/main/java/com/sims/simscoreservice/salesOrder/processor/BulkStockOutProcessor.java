package com.sims.simscoreservice.salesOrder.processor;

import com.sims.common.exceptions.ServiceException;
import com.sims.simscoreservice.exceptions.InventoryException;
import com.sims.simscoreservice.inventory.stockManagement.StockManagementService;
import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.helper.SalesOrderHelper;
import com.sims.simscoreservice.stockMovement.service.StockMovementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Map;

/**
 * Bulk Stock Out Processor
 * Processes multiple order items in a single transaction
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component
@Slf4j
public class BulkStockOutProcessor extends OrderProcessor implements StockOutProcessor {

    public BulkStockOutProcessor(Clock clock, SalesOrderHelper salesOrderHelper,
                                 StockManagementService stockManagementService, StockMovementService stockMovementService) {
        super(clock, salesOrderHelper, stockManagementService, stockMovementService);
    }

    @Override
    @Transactional
    public SalesOrder processStockOut(SalesOrder salesOrder, Map<String, Integer> approvedQuantities, String username) {
        try {
            SalesOrder updatedSalesOrder = processOrder(salesOrder, approvedQuantities, username);

            log.info("[BULK-STOCK-OUT] Order {} processing complete!", updatedSalesOrder.getOrderReference());

            return updatedSalesOrder;

        } catch (InventoryException ie) {
            throw ie;
        } catch (Exception e) {
            log.error("[BULK-STOCK-OUT] Error processing order: {}", e.getMessage());
            throw new ServiceException("Failed to process order", e);
        }
    }
}
