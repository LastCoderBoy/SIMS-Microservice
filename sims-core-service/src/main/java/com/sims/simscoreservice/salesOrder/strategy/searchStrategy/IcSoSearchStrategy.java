package com.sims.simscoreservice.salesOrder.strategy.searchStrategy;

import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ServiceException;
import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.repository.SalesOrderRepository;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * IC (Inventory Context) Sales Order Search Strategy
 * Searches only pending sales orders
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component("icSoSearchStrategy")
@RequiredArgsConstructor
@Slf4j
public class IcSoSearchStrategy implements SoSearchStrategy {

    private final GlobalServiceHelper globalServiceHelper;
    private final SalesOrderRepository salesOrderRepository;

    @Override
    public Page<SalesOrder> searchInSo(String text, int page, int size, String sortBy, String sortDirection) {
        try {
            Pageable pageable = globalServiceHelper.preparePageable(page, size, sortBy, sortDirection);

            log.info("[IC-SO-SEARCH] Searching pending orders with text: '{}'", text);

            return salesOrderRepository.searchInWaitingSalesOrders(text, pageable);

        } catch (DataAccessException dae) {
            log.error("[IC-SO-SEARCH] Database error: {}", dae.getMessage());
            throw new DatabaseException("Failed to search pending orders", dae);
        } catch (Exception e) {
            log.error("[IC-SO-SEARCH] Error searching: {}", e.getMessage());
            throw new ServiceException("Failed to search pending orders", e);
        }
    }
}
