package com.sims.simscoreservice.shared.util;


import com.sims.common.exceptions.InvalidTokenException;
import com.sims.common.exceptions.ResourceNotFoundException;
import com.sims.common.exceptions.ValidationException;
import com.sims.common.utils.TokenUtils;
import com.sims.simscoreservice.product.enums.ProductStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Global Service Helper
 * Shared utility methods across all modules
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component
@Slf4j
public class GlobalServiceHelper {

    /**
     * Check if product status is invalid (cannot be ordered)
     */
    public static boolean amongInvalidStatus(ProductStatus status) {
        return status == ProductStatus.RESTRICTED ||
                status == ProductStatus.ARCHIVED ||
                status == ProductStatus.DISCONTINUED;
    }

    /**
     * Get current timestamp
     */
    public static LocalDateTime now(Clock clock) {
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * Validate order ID exists in database
     */
    @Transactional(readOnly = true)
    public <T> void validateOrderId(Long orderId, JpaRepository<T, Long> repository, String entityName) {
        if (orderId == null || orderId < 1) {
            throw new ValidationException(entityName + " Order ID must be valid and greater than zero");
        }
        if (!repository.existsById(orderId)) {
            throw new ResourceNotFoundException(entityName + " Order with ID " + orderId + " does not exist");
        }
    }

    /**
     * Check if string value exists in enum
     */
    public static <T extends Enum<T>> boolean isInEnum(String value, Class<T> enumClass) {
        try {
            Enum.valueOf(enumClass, value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Check if enum value is valid
     */
    public static <T extends Enum<T>> boolean isInEnum(Enum<T> value, Class<T> enumClass) {
        if (value == null) return false;
        return enumClass.isInstance(value);
    }

    /**
     * Validate and extract JWT token from Authorization header
     */
    public static String validateAndExtractToken(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            log.error("[GLOBAL-HELPER] Invalid or missing Authorization header");
            throw new InvalidTokenException("Invalid Token provided. Please re-login.");
        }
        return TokenUtils.extractToken(authHeader);
    }

    /**
     * Get option date value for sorting/filtering
     */
    public static @Nullable String getOptionDateValue(String optionDate) {
        if (optionDate == null || optionDate.trim().isEmpty()) {
            return null;
        }

        return switch (optionDate.trim().toLowerCase()) {
            case "orderdate" -> "orderDate";
            case "deliverydate" -> "deliveryDate";
            case "estimateddeliverydate" -> "estimatedDeliveryDate";
            default -> throw new IllegalArgumentException("Invalid optionDate value provided: " + optionDate);
        };
    }

    /**
     * Validate pagination parameters
     */
    public void validatePaginationParameters(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
    }

    /**
     * Prepare pageable with sort
     */
    public Pageable preparePageable(int page, int size, String sortBy, String sortDirection) {
        validatePaginationParameters(page, size);

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        return PageRequest.of(page, size, sort);
    }

    /**
     * Normalize and validate option date parameter
     */
    public static String normalizeOptionDate(String optionDate) {
        if (optionDate == null || optionDate.trim().isEmpty()) {
            return null;
        }

        String normalized = optionDate.trim().toLowerCase();

        return switch (normalized) {
            case "orderdate", "order_date", "order-date" -> "orderdate";
            case "deliverydate", "delivery_date", "delivery-date" -> "deliverydate";
            case "estimateddeliverydate", "estimated_delivery_date", "estimated-delivery-date" -> "estimateddeliverydate";
            default -> throw new IllegalArgumentException("Invalid optionDate value: " + optionDate +
                    ". Valid values: orderDate, deliveryDate, estimatedDeliveryDate");
        };
    }

    /**
     * Create header style for Excel Reports
     */
    public static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook. createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors. GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    public static CellStyle createDecimalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }
}
