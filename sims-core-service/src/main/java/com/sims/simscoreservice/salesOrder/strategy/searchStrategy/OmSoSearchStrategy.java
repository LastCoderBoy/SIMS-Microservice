package com.sims.simscoreservice.salesOrder.strategy.searchStrategy;

import com.sims.common.exceptions.ServiceException;
import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.repository.SalesOrderRepository;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * OM (Order Management Context) Sales Order Search Strategy
 * Searches all sales orders (pending + finalized)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component("omSoSearchStrategy")
@RequiredArgsConstructor
@Slf4j
public class OmSoSearchStrategy implements SoSearchStrategy {

    private final GlobalServiceHelper globalServiceHelper;
    private final SalesOrderRepository salesOrderRepository;

    @Override
    public Page<SalesOrder> searchInSo(String text, int page, int size, String sortBy, String sortDirection) {
        try {
            Pageable pageable = globalServiceHelper.preparePageable(page, size, sortBy, sortDirection);

            log.info("[OM-SO-SEARCH] Searching all orders with text: '{}'", text);

            return salesOrderRepository.searchInSalesOrders(text, pageable);

        } catch (Exception e) {
            log.error("[OM-SO-SEARCH] Error searching: {}", e.getMessage());
            throw new ServiceException("Failed to search orders", e);
        }
    }
}
