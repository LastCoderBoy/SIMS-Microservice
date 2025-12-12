package com.sims.simscoreservice.inventory.service.impl;


import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ResourceNotFoundException;
import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.dto.damageLoss.DamageLossDashboardResponse;
import com.sims.simscoreservice.inventory.dto.damageLoss.DamageLossMetrics;
import com.sims.simscoreservice.inventory.dto.damageLoss.DamageLossRequest;
import com.sims.simscoreservice.inventory.dto.damageLoss.DamageLossResponse;
import com.sims.simscoreservice.inventory.entity.DamageLoss;
import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.enums.LossReason;
import com.sims.simscoreservice.inventory.helper.DamageLossHelper;
import com.sims.simscoreservice.inventory.mapper.DamageLossMapper;
import com.sims.simscoreservice.inventory.repository.DamageLossRepository;
import com.sims.simscoreservice.inventory.service.DamageLossService;
import com.sims.simscoreservice.inventory.queryService.DamageLossQueryService;
import com.sims.simscoreservice.inventory.queryService.InventoryQueryService;
import com.sims.simscoreservice.stockManagement.StockManagementService;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.sims.common.constants.AppConstants.DEFAULT_SORT_DIRECTION;

/**
 * Damage/Loss Service Implementation
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DamageLossServiceImpl implements DamageLossService {

    private final Clock clock;
    private final DamageLossRepository damageLossRepository;
    private final DamageLossQueryService damageLossQueryService;
    private final InventoryQueryService inventoryQueryService;
    private final StockManagementService stockManagementService;
    private final DamageLossHelper damageLossHelper;
    private final DamageLossMapper damageLossMapper;
    private final GlobalServiceHelper globalServiceHelper;

    @Override
    @Transactional(readOnly = true)
    public DamageLossDashboardResponse getDashboardData(int page, int size) {
        try {
            // Get metrics
            DamageLossMetrics metrics = damageLossQueryService.getDamageLossMetrics();

            // Get paginated reports & convert
            Page<DamageLoss> reportsPage = damageLossQueryService.getAllDamageLossReports(page, size);
            PaginatedResponse<DamageLossResponse> reports = damageLossHelper.toPaginatedResponse(reportsPage);

            return new DamageLossDashboardResponse(
                    metrics.getTotalReports(),
                    metrics.getTotalItemsLost(),
                    metrics.getTotalLossValue(),
                    reports
            );

        } catch (Exception e) {
            log.error("[DAMAGE-LOSS-SERVICE] Error loading dashboard: {}", e.getMessage());
            throw new ServiceException("Failed to load damage/loss dashboard", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> addDamageLossReport(DamageLossRequest request, String username) {
        try {
            // Validate request
            damageLossHelper.validateRequest(request);

            // Find inventory
            Inventory inventory = inventoryQueryService.getInventoryBySku(request.sku());

            // Validate stock availability
            damageLossHelper.validateStockAvailability(inventory, request.quantityLost());

            // Create damage/loss entity
            DamageLoss damageLoss = damageLossHelper.toEntity(request, inventory, username);
            damageLossRepository.save(damageLoss);
            damageLossRepository.flush();  // Populate timestamps

            // Update inventory stock level
            int remainingStock = inventory.getCurrentStock() - request.quantityLost();
            stockManagementService.updateStockLevels(inventory, remainingStock, null);

            log.info("[DAMAGE-LOSS-SERVICE] Report created for SKU {} by user {}", request.sku(), username);

            return ApiResponse. success("Damage/loss report created for SKU: " + request.sku());

        } catch (ResourceNotFoundException | ValidationException e) {
            throw e;
        } catch (DataAccessException de) {
            log.error("[DAMAGE-LOSS-SERVICE] Database error creating report: {}", de.getMessage());
            throw new DatabaseException("Failed to create damage/loss report", de);
        } catch (Exception e) {
            log.error("[DAMAGE-LOSS-SERVICE] Error creating report: {}", e.getMessage());
            throw new ServiceException("Failed to create damage/loss report", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> updateDamageLossReport(Integer id, DamageLossRequest request) {
        try {
            // Find existing report
            DamageLoss report = damageLossQueryService.findById(id);

            // Check if request is empty
            if (damageLossHelper.isRequestEmpty(request)) {
                throw new ValidationException("At least one field is required to update");
            }

            // Cannot update SKU
            if (request.sku() != null) {
                throw new ValidationException("Cannot update SKU. Please create a new report instead");
            }

            // Update loss date
            if (request.lossDate() != null) {
                if (request.lossDate().isAfter(LocalDateTime.now(clock))) {
                    throw new ValidationException("Loss date cannot be in the future");
                }
                report.setLossDate(request.lossDate());
            }

            // Update quantity lost
            if (request.quantityLost() != null) {
                Inventory inventory = report.getInventory();
                int currentLostQuantity = report.getQuantityLost();
                int newQuantity = request.quantityLost();

                // Validate new quantity
                damageLossHelper.validateStockAvailability(inventory, newQuantity);

                // Calculate stock adjustment
                int stockAdjustment = currentLostQuantity - newQuantity;
                restoreStock(inventory, stockAdjustment);

                // Update report
                report.setQuantityLost(newQuantity);

                // Recalculate loss value
                BigDecimal price = inventory.getProduct().getPrice();
                BigDecimal updatedLossValue = price.multiply(BigDecimal.valueOf(newQuantity));
                report.setLossValue(updatedLossValue);
            }

            // Update reason if provided
            if (request.reason() != null) {
                report.setReason(request.reason());
            }

            damageLossRepository.save(report);

            log.info("[DAMAGE-LOSS-SERVICE] Report {} updated successfully", id);

            return ApiResponse.success("Damage/loss report updated successfully");

        } catch (ResourceNotFoundException | ValidationException e) {
            throw e;
        } catch (DataAccessException de) {
            log.error("[DAMAGE-LOSS-SERVICE] Database error updating report: {}", de.getMessage());
            throw new DatabaseException("Failed to update damage/loss report", de);
        } catch (Exception e) {
            log.error("[DAMAGE-LOSS-SERVICE] Error updating report: {}", e.getMessage());
            throw new ServiceException("Failed to update damage/loss report", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteDamageLossReport(Integer id) {
        try {
            // Find report
            DamageLoss report = damageLossQueryService.findById(id);

            // Deleting the report will Restore the stock
            restoreStock(report.getInventory(), report.getQuantityLost());

            // Delete report
            damageLossRepository.delete(report);

            log.info("[DAMAGE-LOSS-SERVICE] Report {} deleted and stock restored", id);

            return ApiResponse.success("Report deleted and stock restored for SKU: " + report.getInventory().getSku());

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("[DAMAGE-LOSS-SERVICE] Error deleting report: {}", e.getMessage());
            throw new ServiceException("Failed to delete damage/loss report", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<DamageLossResponse> searchReports(String text, int page, int size) {
        try {
            Optional<String> inputText = Optional.ofNullable(text);

            if (inputText.isPresent() && !inputText.get().trim().isEmpty()) {
                Pageable pageable = globalServiceHelper.preparePageable(page, size, "lossDate", DEFAULT_SORT_DIRECTION);
                Page<DamageLoss> searchResults = damageLossRepository.searchReports(
                        inputText.get().trim().toLowerCase(),
                        pageable
                );

                log.info("[DAMAGE-LOSS-SERVICE] Search returned {} results", searchResults.getTotalElements());

                return damageLossHelper.toPaginatedResponse(searchResults);
            }

            log.info("[DAMAGE-LOSS-SERVICE] No search text, returning all reports");
            Page<DamageLoss> allReports = damageLossQueryService.getAllDamageLossReports(page, size);
            return damageLossHelper.toPaginatedResponse(allReports);

        } catch (DataAccessException de) {
            log.error("[DAMAGE-LOSS-SERVICE] Database error searching: {}", de.getMessage());
            throw new DatabaseException("Failed to search damage/loss reports", de);
        } catch (Exception e) {
            log.error("[DAMAGE-LOSS-SERVICE] Error searching: {}", e.getMessage());
            throw new ServiceException("Failed to search damage/loss reports", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<DamageLossResponse> filterReports(LossReason reason, String sortBy,
                                                               String sortDirection, int page, int size) {
        try {
            Pageable pageable = globalServiceHelper.preparePageable(page, size, sortBy, sortDirection);
            Page<DamageLoss> filteredReports = damageLossRepository.findByReason(reason, pageable);

            log.info("[DAMAGE-LOSS-SERVICE] Filter returned {} results", filteredReports.getTotalElements());

            return damageLossHelper.toPaginatedResponse(filteredReports);

        } catch (Exception e) {
            log.error("[DAMAGE-LOSS-SERVICE] Error filtering: {}", e.getMessage());
            throw new ServiceException("Failed to filter damage/loss reports", e);
        }
    }

    @Override
    public void generateReport(HttpServletResponse response, String sortBy, String sortDirection) {
        try {
            // Get all reports
            Sort. Direction direction = sortDirection.equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC
                    : Sort.Direction. ASC;
            Sort sort = Sort.by(direction, sortBy);
            List<DamageLoss> allReports = damageLossRepository.findAll(sort);

            // Generate Excel
            damageLossHelper.generateExcelReport(allReports, response);

            log.info("[DAMAGE-LOSS-SERVICE] Generated report for {} records", allReports.size());

        } catch (Exception e) {
            log.error("[DAMAGE-LOSS-SERVICE] Error generating report: {}", e.getMessage());
            throw new ServiceException("Failed to generate damage/loss report", e);
        }
    }

    /**
     * Restore stock when deleting or updating damage/loss report
     */
    private void restoreStock(Inventory inventory, int quantityToRestore) {
        int updatedStock = inventory.getCurrentStock() + quantityToRestore;
        stockManagementService.updateStockLevels(inventory, updatedStock, null);

        log.info("[DAMAGE-LOSS-SERVICE] Restored {} units to SKU {}.  New stock: {}",
                quantityToRestore, inventory.getSku(), updatedStock);
    }
}
