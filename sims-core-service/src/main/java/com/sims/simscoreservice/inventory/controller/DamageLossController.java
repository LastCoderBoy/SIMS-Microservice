package com.sims.simscoreservice.inventory.controller;

import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.dto.damageLoss.DamageLossDashboardResponse;
import com.sims.simscoreservice.inventory.dto.damageLoss.DamageLossRequest;
import com.sims.simscoreservice.inventory.dto.damageLoss.DamageLossResponse;
import com.sims.simscoreservice.inventory.enums.LossReason;
import com.sims.simscoreservice.inventory.service.DamageLossService;
import com.sims.simscoreservice.shared.util.RoleValidator;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.sims.common.constants.AppConstants.*;

/**
 * Damage/Loss Controller
 * Manages damaged and lost inventory items
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(BASE_INVENTORY_PATH + "/damage-loss")
public class DamageLossController {

    private final DamageLossService damageLossService;
    private final RoleValidator roleValidator;

    /**
     * Get damage/loss dashboard data
     * Returns: metrics + paginated reports
     */
    @GetMapping
    public ResponseEntity<DamageLossDashboardResponse> getDashboardData(
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[DAMAGE-LOSS-CONTROLLER] Get dashboard data by user: {}", userId);

        DamageLossDashboardResponse dashboardResponse = damageLossService.getDashboardData(page, size);

        return ResponseEntity.ok(dashboardResponse);
    }

    /**
     * Add damage/loss report
     * Records damaged or lost inventory and updates stock
     * Only ADMIN/MANAGER can add
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addDamageLossReport(
            @Valid @RequestBody DamageLossRequest request,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        log.info("[DAMAGE-LOSS-CONTROLLER] Add report for SKU {} by user: {}", request.sku(), userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER");

        ApiResponse<Void> response = damageLossService.addDamageLossReport(request, userId);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update damage/loss report
     * Can update: quantity lost, reason, loss date
     * Cannot update: SKU
     * Only ADMIN/MANAGER can update
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateDamageLossReport(
            @PathVariable Integer id,
            @RequestBody DamageLossRequest request,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        log.info("[DAMAGE-LOSS-CONTROLLER] Update report {} by user: {}", id, userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER");

        ApiResponse<Void> response = damageLossService.updateDamageLossReport(id, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete damage/loss report
     * Restores stock to inventory
     * Only ADMIN can delete
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDamageLossReport(
            @PathVariable Integer id,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        log.info("[DAMAGE-LOSS-CONTROLLER] Delete report {} by user: {}", id, userId);

        // Check authorization (only ADMIN)
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN");

        ApiResponse<Void> response = damageLossService.deleteDamageLossReport(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Search damage/loss reports
     * Searches by: Product Name, SKU
     */
    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<DamageLossResponse>> searchReports(
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[DAMAGE-LOSS-CONTROLLER] Search reports with text '{}' by user: {}", text, userId);

        PaginatedResponse<DamageLossResponse> results = damageLossService.searchReports(text, page, size);

        return ResponseEntity.ok(results);
    }

    /**
     * Filter damage/loss reports by reason
     * Uses LossReason enum with converter
     *
     * @param reason LossReason enum (LOST, DAMAGED, SUPPLIER_FAULT, TRANSPORT_DAMAGE, MISPLACED)
     */
    @GetMapping("/filter")
    public ResponseEntity<PaginatedResponse<DamageLossResponse>> filterReports(
            @RequestParam LossReason reason,
            @RequestParam(defaultValue = "lossDate") String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[DAMAGE-LOSS-CONTROLLER] Filter by reason '{}' by user: {}", reason, userId);

        PaginatedResponse<DamageLossResponse> results =
                damageLossService. filterReports(reason, sortBy, sortDirection, page, size);

        return ResponseEntity.ok(results);
    }

    /**
     * Generate Excel report for damage/loss records
     */
    @GetMapping("/report")
    public void generateReport(
            HttpServletResponse response,
            @RequestParam(defaultValue = "lossDate") String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[DAMAGE-LOSS-CONTROLLER] Generate report by user: {}", userId);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=DamageLossReport.xlsx");

        damageLossService.generateReport(response, sortBy, sortDirection);
    }
}
