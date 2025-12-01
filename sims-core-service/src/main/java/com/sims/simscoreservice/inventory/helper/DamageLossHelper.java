package com.sims.simscoreservice.inventory.helper;

import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.dto.damageLoss.DamageLossRequest;
import com.sims.simscoreservice.inventory.dto.damageLoss.DamageLossResponse;
import com.sims.simscoreservice.inventory.entity.DamageLoss;
import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.mapper.DamageLossMapper;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Damage/Loss Helper
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DamageLossHelper {

    private final Clock clock;
    private final DamageLossMapper damageLossMapper;

    public boolean isRequestEmpty(DamageLossRequest request) {
        return request.sku() == null &&
                request.lossDate() == null &&
                request.quantityLost() == null &&
                request.reason() == null;
    }


    public PaginatedResponse<DamageLossResponse> toPaginatedResponse(Page<DamageLoss> damageLossPage) {
        List<DamageLossResponse> content = damageLossMapper.toResponseList(damageLossPage.getContent());

        return PaginatedResponse.<DamageLossResponse>builder()
                .content(content)
                .totalPages(damageLossPage.getTotalPages())
                .totalElements(damageLossPage.getTotalElements())
                .currentPage(damageLossPage.getNumber())
                .pageSize(damageLossPage.getSize())
                .build();
    }

    /**
     * Convert Request to Entity
     */
    public DamageLoss toEntity(DamageLossRequest request, Inventory inventory, String username) {
        BigDecimal price = inventory.getProduct().getPrice();
        BigDecimal lossValue = price.multiply(BigDecimal.valueOf(request.quantityLost()));

        DamageLoss damageLoss = new DamageLoss();
        damageLoss.setInventory(inventory);
        damageLoss.setQuantityLost(request.quantityLost());
        damageLoss.setReason(request.reason());
        damageLoss.setLossValue(lossValue);
        damageLoss.setLossDate(request.lossDate() != null ?  request.lossDate() : LocalDateTime.now(clock));
        damageLoss.setRecordedBy(username);

        return damageLoss;
    }

    /**
     * Validate stock availability
     */
    public void validateStockAvailability(Inventory inventory, Integer lostQuantity) {
        if (lostQuantity == null || lostQuantity <= 0) {
            throw new ValidationException("Lost quantity must be greater than zero");
        }

        if (inventory.getCurrentStock() < lostQuantity) {
            throw new ValidationException(
                    String.format("Insufficient stock.  Available: %d, Requested: %d",
                            inventory.getCurrentStock(), lostQuantity)
            );
        }
    }

    /**
     * Validate damage/loss request
     */
    public void validateRequest(DamageLossRequest request) {
        if (request == null) {
            throw new ValidationException("Request cannot be null");
        }

        List<String> errors = new ArrayList<>();

        if (request.sku() == null || request.sku().trim().isEmpty()) {
            errors.add("SKU is required");
        }

        if (request.quantityLost() == null || request.quantityLost() <= 0) {
            errors.add("Quantity lost must be greater than zero");
        }

        if (request.reason() == null) {
            errors.add("Loss reason is required");
        }

        if (request.lossDate() != null && request.lossDate().isAfter(LocalDateTime.now(clock))) {
            errors.add("Loss date cannot be in the future");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(String.join("; ", errors));
        }
    }

    /**
     * Generate Excel report for damage/loss records
     */
    public void generateExcelReport(List<DamageLoss> reports, HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Damage Loss Reports");

            // Create header style
            CellStyle headerStyle = GlobalServiceHelper.createHeaderStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Report ID", "SKU", "Product Name", "Category", "Quantity Lost",
                    "Loss Value", "Reason", "Loss Date", "Recorded By", "Created At"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            for (DamageLoss report : reports) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(report.getId());
                row.createCell(1).setCellValue(report.getInventory().getSku());
                row.createCell(2).setCellValue(report.getInventory().getProduct().getName());
                row.createCell(3).setCellValue(report.getInventory().getProduct().getCategory().name());
                row.createCell(4).setCellValue(report.getQuantityLost());
                row.createCell(5).setCellValue(report.getLossValue().doubleValue());
                row.createCell(6).setCellValue(report.getReason().name());
                row.createCell(7).setCellValue(report.getLossDate().toString());
                row.createCell(8).setCellValue(report.getRecordedBy() != null ? report.getRecordedBy() : "");
                row.createCell(9).setCellValue(report.getCreatedAt().toString());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to response
            workbook.write(response.getOutputStream());

        } catch (IOException e) {
            log.error("[DAMAGE-LOSS-SERVICE] Failed to generate Excel report: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }
}
