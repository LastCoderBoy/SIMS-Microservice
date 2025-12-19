package com.sims.simscoreservice.product.helper;

import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.product.dto.ProductRequest;
import com.sims.simscoreservice.product.dto.ProductResponse;
import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.product.enums.ProductStatus;
import com.sims.simscoreservice.product.mapper.ProductMapper;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Product Helper
 * Utility methods for product operations
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductHelper {

    private static final Pattern LOCATION_PATTERN = Pattern.compile("^[A-Za-z]\\d{1,2}-\\d{3}$");

    private final ProductMapper productMapper;

    public void validateLocationFormat(String location) {
        if (!LOCATION_PATTERN.matcher(location).matches()) {
            throw new ValidationException("PM (updateProduct): Invalid location format. Expected format: section-shelf (e.g., A1-101). ");
        }
    }

    /**
     * Validate product request
     */
    public void validateProduct(ProductRequest request) throws ValidationException {
        StringBuilder errors = new StringBuilder();

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            errors.append("Name is required. ");
        }

        if (request.getCategory() == null) {
            errors.append("Category is required. ");
        }

        if (request.getStatus() == null) {
            errors.append("Status is required. ");
        }

        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            errors.append("Valid price is required. ");
        }

        if (request.getLocation() == null || request.getLocation().isEmpty()) {
            errors.append("Location is required. ");
        } else if (!LOCATION_PATTERN.matcher(request.getLocation()).matches()) {
            errors.append("Invalid location format. Expected: A1-123 (Section-Shelf). ");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Invalid product data: " + errors.toString().trim());
        }
    }

    /**
     * Check if all fields in request are null
     */
    public boolean isAllFieldsNull(ProductRequest request) {
        return request.getName() == null &&
                request.getCategory() == null &&
                request.getPrice() == null &&
                request.getStatus() == null &&
                request.getLocation() == null;
    }


    public boolean validateStatusBeforeAdding(ProductStatus currentStatus, ProductStatus newStatus){
        if(currentStatus.equals(ProductStatus.PLANNING) || currentStatus.equals(ProductStatus.ARCHIVED)
                || currentStatus.equals(ProductStatus.DISCONTINUED)){
            return !newStatus.equals(ProductStatus.PLANNING);
        }
        return false;
    }

    /**
     * Convert Page<Product> to PaginatedResponse<ProductResponse>
     */
    public PaginatedResponse<ProductResponse> toPaginatedResponse(Page<Product> productPage) {
        List<ProductResponse> content = productMapper.toResponseList(productPage.getContent());

        return PaginatedResponse.<ProductResponse>builder()
                .content(content)
                .totalPages(productPage.getTotalPages())
                .totalElements(productPage.getTotalElements())
                .currentPage(productPage.getNumber())
                .pageSize(productPage.getSize())
                .build();
    }



    /**
     * Generate Excel report for products
     */
    public void generateExcelReport(List<Product> products, HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Products");

            // Create header style
            CellStyle headerStyle = GlobalServiceHelper.createHeaderStyle(workbook);

            // Create price style
            CellStyle priceStyle = GlobalServiceHelper.createDecimalStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Product ID", "Name", "Category", "Location", "Price", "Status", "Created At"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0). setCellValue(product.getProductId());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getCategory().name());
                row.createCell(3).setCellValue(product.getLocation());
                Cell priceCell = row.createCell(4);
                priceCell.setCellValue(product.getPrice().doubleValue());
                priceCell.setCellStyle(priceStyle);
                row.createCell(5).setCellValue(product.getStatus().name());
                row.createCell(6).setCellValue(product.getCreatedAt().toString());
            }

            // Auto-size columns
            for (int i = 0; i < headers. length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to response
            workbook.write(response.getOutputStream());

        } catch (IOException e) {
            log.error("[PRODUCT-HELPER] Failed to generate Excel report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }
}
