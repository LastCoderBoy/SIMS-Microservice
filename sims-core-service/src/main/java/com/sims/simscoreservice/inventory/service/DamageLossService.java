package com.sims.simscoreservice.inventory.service;


import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.dto.damageLoss.DamageLossDashboardResponse;
import com.sims.simscoreservice.inventory.dto.damageLoss.DamageLossRequest;
import com.sims.simscoreservice.inventory.dto.damageLoss.DamageLossResponse;
import com.sims.simscoreservice.inventory.enums.LossReason;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Damage/Loss Service Interface
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface DamageLossService {

    /**
     * Get damage/loss dashboard data
     */
    DamageLossDashboardResponse getDashboardData(int page, int size);

    ApiResponse<Void> addDamageLossReport(DamageLossRequest request, String username);

    ApiResponse<Void> updateDamageLossReport(Integer id, DamageLossRequest request);

    /**
     * Delete damage/loss report (restores stock)
     */
    ApiResponse<Void> deleteDamageLossReport(Integer id);

    PaginatedResponse<DamageLossResponse> searchReports(String text, int page, int size);

    /**
     * Filter damage/loss reports by reason
     */
    PaginatedResponse<DamageLossResponse> filterReports(LossReason reason, String sortBy, String sortDirection, int page, int size);

    void generateReport(HttpServletResponse response, String sortBy, String sortDirection);
}
