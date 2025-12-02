package com.sims.simscoreservice.purchaseOrder.strategy.searchStrategy;


import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ServiceException;
import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import com.sims.simscoreservice.purchaseOrder.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * IC (Inventory Context) Purchase Order Search Strategy
 * Searches only pending purchase orders
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component("icPoSearchStrategy")
@RequiredArgsConstructor
@Slf4j
public class IcPoSearchStrategy implements PoSearchStrategy {

    private final PurchaseOrderRepository purchaseOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrder> searchInPos(String text, int page, int size, String sortBy, String sortDirection) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy). descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            log.info("[IC-PO-SEARCH] Searching pending orders with text: '{}'", text);

            return purchaseOrderRepository.searchInPendingOrders(text. trim(). toLowerCase(), pageable);

        } catch (DataAccessException dae) {
            log.error("[IC-PO-SEARCH] Database error: {}", dae.getMessage());
            throw new DatabaseException("Failed to search pending orders", dae);
        } catch (Exception e) {
            log.error("[IC-PO-SEARCH] Error searching: {}", e.getMessage());
            throw new ServiceException("Failed to search pending orders", e);
        }
    }
}
