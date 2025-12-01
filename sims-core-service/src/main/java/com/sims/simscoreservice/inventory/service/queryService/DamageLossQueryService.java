package com.sims.simscoreservice.inventory.service.queryService;


import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ResourceNotFoundException;
import com.sims.common.exceptions.ServiceException;
import com.sims.simscoreservice.inventory.dto.damageLoss.DamageLossMetrics;
import com.sims.simscoreservice.inventory.entity.DamageLoss;
import com.sims.simscoreservice.inventory.repository.DamageLossRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Damage/Loss Query Service
 * Read-only operations for damage/loss reports
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DamageLossQueryService {

    private final DamageLossRepository damageLossRepository;

    /**
     * Find damage/loss report by ID
     */
    @Transactional(readOnly = true)
    public DamageLoss findById(Integer id) {
        return damageLossRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Damage/Loss report not found with ID: " + id));
    }

    /**
     * Get all damage/loss reports with pagination
     */
    @Transactional(readOnly = true)
    public Page<DamageLoss> getAllDamageLossReports(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("lossDate").descending());
            return damageLossRepository.findAll(pageable);
        } catch (DataAccessException de) {
            log.error("[DAMAGE-LOSS-QUERY] Database error: {}", de.getMessage());
            throw new DatabaseException("Failed to retrieve damage/loss reports", de);
        } catch (Exception e) {
            log.error("[DAMAGE-LOSS-QUERY] Error retrieving reports: {}", e.getMessage());
            throw new ServiceException("Failed to retrieve damage/loss reports", e);
        }
    }

    /**
     * Get damage/loss metrics (total reports, total items lost, total loss value)
     */
    @Transactional(readOnly = true)
    public DamageLossMetrics getDamageLossMetrics() {
        try {
            return damageLossRepository.getDamageLossMetrics();
        } catch (DataAccessException de) {
            log.error("[DAMAGE-LOSS-QUERY] Database error getting metrics: {}", de.getMessage());
            throw new DatabaseException("Failed to retrieve damage/loss metrics", de);
        } catch (Exception e) {
            log.error("[DAMAGE-LOSS-QUERY] Error getting metrics: {}", e.getMessage());
            throw new ServiceException("Failed to retrieve damage/loss metrics", e);
        }
    }

    /**
     * Count total damaged products for the Report & Analytics section
     */
    @Transactional(readOnly = true)
    public Long countTotalDamagedProducts() {
        try {
            return damageLossRepository.countTotalDamagedProducts();
        } catch (DataAccessException de) {
            log.error("[DAMAGE-LOSS-QUERY] Database error counting damaged products: {}", de.getMessage());
            throw new DatabaseException("Failed to count damaged products", de);
        }
    }
}
