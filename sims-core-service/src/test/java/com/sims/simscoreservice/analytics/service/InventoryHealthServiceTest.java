package com.sims.simscoreservice.analytics.service;

import com.sims.common.exceptions.DatabaseException;
import com.sims.simscoreservice.analytics.dto.InventoryReportMetrics;
import com.sims.simscoreservice.analytics.service.impl.InventoryHealthServiceImpl;
import com.sims.simscoreservice.inventory.repository.InventoryRepository;
import org.junit.jupiter. api.DisplayName;
import org.junit. jupiter.api.Test;
import org.junit.jupiter.api. extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.math. BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Inventory Health Service Tests
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Inventory Health Service Tests")
class InventoryHealthServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryHealthServiceImpl inventoryHealthService;

    // ========================================
    // INVENTORY HEALTH TESTS
    // ========================================

    @Test
    @DisplayName("Should return inventory health metrics")
    void getInventoryHealth_Success() {
        // Arrange
        InventoryReportMetrics mockMetrics = InventoryReportMetrics.builder()
                .totalStockValueAtRetail(BigDecimal.valueOf(500000.00))
                .totalStockQuantity(1000L)
                .totalReservedStock(200L)
                .availableStock(800L)
                .inStockItems(120L)
                .lowStockItems(15L)
                .outOfStockItems(5L)
                .build();

        when(inventoryRepository.getInventoryReportMetrics()).thenReturn(mockMetrics);

        // Act
        InventoryReportMetrics result = inventoryHealthService.getInventoryHealth();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalStockQuantity()).isEqualTo(1000L);
        assertThat(result.getTotalReservedStock()).isEqualTo(200L);
        assertThat(result.getAvailableStock()).isEqualTo(800L);
        assertThat(result.getHealthStatus()).isEqualTo("EXCELLENT");

        verify(inventoryRepository).getInventoryReportMetrics();
    }

    @Test
    @DisplayName("Should calculate stock utilization correctly")
    void getInventoryHealth_StockUtilization_CorrectCalculation() {
        // Arrange
        InventoryReportMetrics mockMetrics = InventoryReportMetrics.builder()
                .totalStockQuantity(1000L)
                .totalReservedStock(200L)
                .inStockItems(200L)
                .lowStockItems(0L)
                .outOfStockItems(0L)
                .build();

        when(inventoryRepository.getInventoryReportMetrics()).thenReturn(mockMetrics);

        // Act
        InventoryReportMetrics result = inventoryHealthService.getInventoryHealth();

        // Assert
        // (200 / 1000) * 100 = 20%
        assertThat(result.getStockUtilization()).isEqualTo(20.0);
    }

    @Test
    @DisplayName("Should calculate health score correctly - EXCELLENT")
    void getInventoryHealth_HealthScore_Excellent() {
        // Arrange
        InventoryReportMetrics mockMetrics = InventoryReportMetrics.builder()
                .inStockItems(95L)
                .lowStockItems(3L)
                .outOfStockItems(2L)
                .build();

        when(inventoryRepository.getInventoryReportMetrics()).thenReturn(mockMetrics);

        // Act
        InventoryReportMetrics result = inventoryHealthService.getInventoryHealth();

        // Assert
        assertThat(result. getHealthScore()).isGreaterThanOrEqualTo(90.0);
        assertThat(result.getHealthStatus()).isEqualTo("EXCELLENT");
    }

    @Test
    @DisplayName("Should calculate health score correctly - POOR")
    void getInventoryHealth_HealthScore_Poor() {
        // Arrange
        InventoryReportMetrics mockMetrics = InventoryReportMetrics.builder()
                .inStockItems(30L)
                .lowStockItems(20L)
                .outOfStockItems(20L)
                .build();

        when(inventoryRepository.getInventoryReportMetrics()).thenReturn(mockMetrics);

        // Act
        InventoryReportMetrics result = inventoryHealthService.getInventoryHealth();

        // Assert
        assertThat(result.getHealthScore()).isLessThan(60.0);
        assertThat(result.getHealthStatus()).isIn("POOR", "CRITICAL");
    }

    @Test
    @DisplayName("Should throw DatabaseException on repository error")
    void getInventoryHealth_DatabaseError_ThrowsException() {
        // Arrange
        when(inventoryRepository.getInventoryReportMetrics())
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThatThrownBy(() -> inventoryHealthService.getInventoryHealth())
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Failed to calculate inventory health");
    }

    // ========================================
    // STOCK VALUE TESTS
    // ========================================

    @Test
    @DisplayName("Should calculate inventory stock value at retail")
    void calculateInventoryStockValue_Success() {
        // Arrange
        BigDecimal expectedValue = BigDecimal.valueOf(500000.00);
        when(inventoryRepository.getInventoryStockValueAtRetail()).thenReturn(expectedValue);

        // Act
        BigDecimal result = inventoryHealthService.calculateInventoryStockValueAtRetail();

        // Assert
        assertThat(result).isEqualByComparingTo(expectedValue);
        verify(inventoryRepository).getInventoryStockValueAtRetail();
    }

    @Test
    @DisplayName("Should throw DatabaseException when calculating stock value fails")
    void calculateInventoryStockValue_DatabaseError_ThrowsException() {
        // Arrange
        when(inventoryRepository.getInventoryStockValueAtRetail())
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThatThrownBy(() -> inventoryHealthService.calculateInventoryStockValueAtRetail())
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Failed to calculate inventory stock value");
    }
}
