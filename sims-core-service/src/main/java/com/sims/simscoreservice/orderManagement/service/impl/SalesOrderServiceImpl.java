package com.sims.simscoreservice.orderManagement.service.impl;

import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ResourceNotFoundException;
import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.exceptions.InsufficientStockException;
import com.sims.simscoreservice.orderManagement.service.SalesOrderService;
import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.product.services.queryService.ProductQueryService;
import com.sims.simscoreservice.qrCode.service.SalesOrderQrCodeService;
import com.sims.simscoreservice.salesOrder.dto.DetailedSalesOrderView;
import com.sims.simscoreservice.salesOrder.dto.SalesOrderAdjustments;
import com.sims.simscoreservice.salesOrder.dto.SalesOrderRequest;
import com.sims.simscoreservice.salesOrder.dto.SummarySalesOrderView;
import com.sims.simscoreservice.salesOrder.dto.orderItem.BulkOrderItemsRequestDto;
import com.sims.simscoreservice.salesOrder.dto.orderItem.OrderItemRequest;
import com.sims.simscoreservice.salesOrder.entity.OrderItem;
import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.qrCode.entity.SalesOrderQRCode;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import com.sims.simscoreservice.salesOrder.helper.SalesOrderHelper;
import com.sims.simscoreservice.salesOrder.queryService.SalesOrderQueryService;
import com.sims.simscoreservice.salesOrder.repository.SalesOrderRepository;
import com.sims.simscoreservice.salesOrder.strategy.SalesOrderSearchService;
import com.sims.simscoreservice.stockManagement.StockManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sales Order Service Implementation
 * Handles sales order business logic with stock management
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SalesOrderServiceImpl implements SalesOrderService {

    // ========== Components ==========
    private final SalesOrderHelper salesOrderHelper;

    // ========== Services ==========
    private final SalesOrderQueryService salesOrderQueryService;
    private final SalesOrderSearchService salesOrderSearchService;
    private final ProductQueryService productQueryService;
    private final StockManagementService stockManagementService;
    private final SalesOrderQrCodeService salesOrderQrCodeService;

    // ========== Repositories ==========
    private final SalesOrderRepository salesOrderRepository;


    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummarySalesOrderView> getAllSummarySalesOrders(
            String sortBy, String sortDirection, int page, int size) {

        log.debug("[SO-SERVICE] Getting all sales orders (page={}, size={})", page, size);
        return salesOrderQueryService.getAllSummarySalesOrders(sortBy, sortDirection, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public DetailedSalesOrderView getDetailsForSalesOrderId(Long orderId) {
        log.debug("[SO-SERVICE] Getting details for sales order ID: {}", orderId);
        return salesOrderQueryService.getDetailsForSalesOrder(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummarySalesOrderView> searchInSalesOrders(
            String text, int page, int size, String sortBy, String sortDirection) {

        log.debug("[SO-SERVICE] Searching sales orders with text: '{}'", text);
        return salesOrderSearchService.searchAll(text, page, size, sortBy, sortDirection);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummarySalesOrderView> filterSalesOrders(
            SalesOrderStatus status, String optionDate, LocalDate startDate, LocalDate endDate,
            int page, int size, String sortBy, String sortDirection) {

        log.debug("[SO-SERVICE] Filtering sales orders (status={}, dateOption={})", status, optionDate);
        return salesOrderSearchService.filterAll(status, optionDate, startDate, endDate,
                page, size, sortBy, sortDirection);
    }

    @Override
    @Transactional
    public ApiResponse<String> createSalesOrder(@Valid SalesOrderRequest request, String userId) {
        List<OrderItem> reservedItems = new ArrayList<>();
        String qrCodeS3Key = null;
        boolean success = false;

        try {
            log.info("[SO-SERVICE] Creating sales order for user: {}", userId);

            salesOrderHelper.validateSalesOrderItems(request.getOrderItems());

            String orderReference = generateOrderReference(LocalDate.now());
            log.debug("[SO-SERVICE] Generated order reference: {}", orderReference);

            // Create QR code and upload to S3
            SalesOrderQRCode qrCode = salesOrderQrCodeService.generateAndLinkQrCode(orderReference);
            qrCodeS3Key = "qr-codes/" + orderReference + ".png"; // Track for rollback
            log.debug("[SO-SERVICE] QR code created and uploaded to S3: {}", qrCodeS3Key);

            // Create the entity and the set the QR Code as well
            SalesOrder salesOrder = new SalesOrder(request, orderReference, userId, qrCode);

            // Process items with stock reservation
            populateSalesOrderWithItems(salesOrder, request.getOrderItems());
            reservedItems.addAll(salesOrder.getItems()); // Track for rollback
            log.debug("[SO-SERVICE] Reserved stock for {} items", reservedItems.size());

            salesOrderRepository.save(salesOrder);
            success = true;

            log.info("[SO-SERVICE] Sales order created successfully: {}", orderReference);
            return ApiResponse.success("Sales order created successfully with reference: " + orderReference);

        } catch (ValidationException | InsufficientStockException | ResourceNotFoundException e) {
            log.error("[SO-SERVICE] Validation/Stock error creating order: {}", e.getMessage());
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.error("[SO-SERVICE] Data integrity error:  {}", e.getMessage());
            throw new DatabaseException("Failed to save order due to data integrity violation", e);
        } catch (DataAccessException e) {
            log.error("[SO-SERVICE] Database error: {}", e.getMessage());
            throw new DatabaseException("Failed to save order to database", e);
        } catch (Exception e) {
            log.error("[SO-SERVICE] Unexpected error creating order: {}", e.getMessage(), e);
            throw new ServiceException("Internal error creating sales order", e);
        } finally {
            // Rollback on failure
            if (!success) {
                log.warn("[SO-SERVICE] Rolling back failed order creation");

                // Rollback stock reservations
                if (!reservedItems.isEmpty()) {
                    rollbackReservations(reservedItems);
                }

                // Rollback S3 upload
                if (qrCodeS3Key != null) {
                    rollbackS3Upload(qrCodeS3Key);
                }
            }
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> updateSalesOrder(Long orderId, SalesOrderRequest request, String userId) {
        try {
            log.info("[SO-SERVICE] Updating sales order {} by user: {}", orderId, userId);

            salesOrderHelper.validateSoRequestForUpdate(request); // null check

            SalesOrder salesOrder = salesOrderQueryService.findById(orderId);

            // Check if finalized
            if (salesOrder.isFinalized()) {
                log.warn("[SO-SERVICE] Order {} is finalized, cannot update", orderId);
                throw new ValidationException("Order is already processed and cannot be updated");
            }

            updateBaseFieldsIfProvided(
                    salesOrder,
                    request.getDestination(),
                    request.getCustomerName()
            );

            // Update item quantities
            updateItemQuantity(salesOrder, request.getOrderItems());

            salesOrder.setUpdatedBy(userId);

            salesOrderRepository.save(salesOrder);

            log.info("[SO-SERVICE] Sales order {} updated successfully", salesOrder.getOrderReference());
            return ApiResponse.success("Sales order updated successfully: " + salesOrder.getOrderReference());

        } catch (ResourceNotFoundException | ValidationException | InsufficientStockException e) {
            throw e;
        } catch (Exception e) {
            log.error("[SO-SERVICE] Unexpected error updating order: {}", e.getMessage(), e);
            throw new ServiceException("Internal error updating sales order", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> addItemsToSalesOrder(Long orderId, @Valid BulkOrderItemsRequestDto request, String userId) {
        List<OrderItem> newlyAddedItems = new ArrayList<>();
        boolean success = false;

        try {
            log.info("[SO-SERVICE] Adding items to sales order {} by user: {}", orderId, userId);

            SalesOrder salesOrder = salesOrderQueryService.findById(orderId);

            if (salesOrder.isFinalized()) {
                log.warn("[SO-SERVICE] Order {} is finalized, cannot add items", orderId);
                throw new ValidationException("Order is already processed and cannot be modified");
            }

            salesOrderHelper.validateSalesOrderItems(request.getOrderItems());

            // Track initial item count
            int initialItemCount = salesOrder.getItems().size();

            // Add items with stock reservation
            populateSalesOrderWithItems(salesOrder, request.getOrderItems());

            // Track newly added items for rollback
            if (salesOrder.getItems().size() > initialItemCount) {
                newlyAddedItems.addAll(salesOrder.getItems().subList(initialItemCount, salesOrder.getItems().size()));
            }

            salesOrder.setUpdatedBy(userId);
            salesOrderRepository.save(salesOrder);
            success = true;

            log.info("[SO-SERVICE] Added {} items to order {}", newlyAddedItems.size(), salesOrder.getOrderReference());
            return ApiResponse.success("Items added successfully to order: " + salesOrder.getOrderReference());

        } catch (ValidationException | InsufficientStockException | ResourceNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("[SO-SERVICE] Database error adding items: {}", e.getMessage());
            throw new DatabaseException("Failed to add items to order", e);
        } catch (Exception e) {
            log.error("[SO-SERVICE] Unexpected error adding items: {}", e.getMessage(), e);
            throw new ServiceException("Internal error adding items to order", e);
        } finally {
            // Rollback on failure
            if (!success && !newlyAddedItems.isEmpty()) {
                log.warn("[SO-SERVICE] Rolling back {} newly added items", newlyAddedItems.size());
                rollbackReservations(newlyAddedItems);
            }
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> removeItemFromSalesOrder(Long orderId, Long itemId, String userId) {
        try {
            log.info("[SO-SERVICE] Removing item {} from order {} by user: {}", itemId, orderId, userId);

            SalesOrder salesOrder = salesOrderQueryService.findById(orderId);

            if (salesOrder.isFinalized()) {
                log.warn("[SO-SERVICE] Order {} is finalized, cannot remove items", orderId);
                throw new ValidationException("Order is finalized and cannot be modified");
            }

            // Find item to remove
            OrderItem itemToRemove = salesOrder.getItems().stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Item " + itemId + " not found in order " + salesOrder.getOrderReference()));

            if (itemToRemove.isFinalized()) {
                log.warn("[SO-SERVICE] Item {} is finalized, cannot remove", itemId);
                throw new ValidationException("Order item is finalized and cannot be removed");
            }

            stockManagementService.releaseReservation(
                    itemToRemove.getProduct().getProductId(),
                    itemToRemove.getQuantity()
            );
            log.debug("[SO-SERVICE] removeItemFromSalesOrder() - Released {} units of product {}",
                    itemToRemove.getQuantity(), itemToRemove.getProduct().getProductId());

            salesOrder.removeOrderItem(itemToRemove);

            // 7.Update order status if no items left
            salesOrderHelper.updateSoStatusBasedOnItemQuantity(salesOrder);

            salesOrder.setUpdatedBy(userId);
            salesOrderRepository.save(salesOrder);

            log.info("[SO-SERVICE] Item {} removed from order {}", itemId, salesOrder.getOrderReference());
            return ApiResponse.success("Item removed successfully from order: " + salesOrder.getOrderReference());

        } catch (ResourceNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("[SO-SERVICE] Unexpected error removing item:  {}", e.getMessage(), e);
            throw new ServiceException("Internal error removing item from order", e);
        }
    }

    // ========================================
    // PRIVATE HELPER METHODS
    // ========================================

    /**
     * Populate sales order with items (reserves stock)
     */
    private void populateSalesOrderWithItems(SalesOrder salesOrder, List<OrderItemRequest> itemRequests) {
        for (OrderItemRequest itemDto : itemRequests) {
            Product product = productQueryService.findById(itemDto.getProductId());

            // Validate product status
            if (!product.isValidForSale()) {
                throw new ValidationException(
                        "Product '" + product.getName() + "' (ID: " + product.getProductId() + ") is not active");
            }

            // Reserve stock (throws exception if insufficient)
            stockManagementService.reserveStock(product.getProductId(), itemDto.getQuantity());
            log.debug("[SO-SERVICE] Reserved {} units of product {}", itemDto.getQuantity(), product.getProductId());

            // Create and add order item
            OrderItem orderItem = new OrderItem(product, itemDto.getQuantity());
            salesOrder.addOrderItem(orderItem);
        }
    }

    private void updateBaseFieldsIfProvided(
            SalesOrder salesOrder, @Nullable String destination, @Nullable String customerName) {

        Optional.ofNullable(destination)
                .filter(dest -> !dest.trim().isEmpty())
                .ifPresent(dest -> {
                    salesOrder.setDestination(dest.trim());
                    log.debug("[SO-SERVICE] Updated destination to: {}", dest);
                });

        Optional.ofNullable(customerName)
                .filter(name -> !name.trim().isEmpty())
                .ifPresent(name -> {
                    salesOrder.setCustomerName(name.trim());
                    log.debug("[SO-SERVICE] Updated customer name to: {}", name);
                });
    }

    /**
     * Update item quantities with stock adjustment
     */
    private void updateItemQuantity(SalesOrder salesOrder, List<OrderItemRequest> itemRequests) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            return; // Nothing to update
        }

        // Create map of existing items
        Map<String, OrderItem> existingItemsMap = salesOrder.getItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getProduct().getProductId(),
                        item -> item
                ));

        // Track adjustments for rollback
        List<SalesOrderAdjustments> stockAdjustments = new ArrayList<>();

        try {
            for (OrderItemRequest itemDto : itemRequests) {
                String productId = itemDto.getProductId();

                if (!existingItemsMap.containsKey(productId)) {
                    continue; // Item not in order, skip
                }

                OrderItem existingItem = existingItemsMap.get(productId);
                int quantityDifference = itemDto.getQuantity() - existingItem.getQuantity();

                if (quantityDifference == 0) {
                    continue; // No change
                }

                if (quantityDifference > 0) {
                    // Need to reserve more stock
                    stockManagementService.reserveStock(productId, quantityDifference);
                    stockAdjustments.add(new SalesOrderAdjustments(productId, quantityDifference, true));
                    log.debug("[SO-SERVICE] Reserved additional {} units of product {}", quantityDifference, productId);
                } else {
                    // Need to release excess stock
                    int releaseAmount = Math.abs(quantityDifference);
                    stockManagementService.releaseReservation(productId, releaseAmount);
                    stockAdjustments.add(new SalesOrderAdjustments(productId, releaseAmount, false));
                    log.debug("[SO-SERVICE] Released {} units of product {}", releaseAmount, productId);
                }

                // Update item quantity and price
                existingItem.setQuantity(itemDto.getQuantity());
                existingItem.setOrderPrice(
                        existingItem.getProduct().getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()))
                );
            }

            log.info("[SO-SERVICE] Successfully updated order items with {} stock adjustments", stockAdjustments.size());

        } catch (Exception e) {
            // Rollback stock adjustments
            log.warn("[SO-SERVICE] Error updating quantities, rolling back {} adjustments", stockAdjustments.size());

            Collections.reverse(stockAdjustments);
            for (SalesOrderAdjustments adjustment : stockAdjustments) {
                try {
                    if (adjustment.isWasReserved()) {
                        // Rollback reservation by releasing
                        stockManagementService.releaseReservation(adjustment.getProductId(), adjustment.getQuantity());
                    } else {
                        // Rollback release by reserving back
                        stockManagementService.reserveStock(adjustment.getProductId(), adjustment.getQuantity());
                    }
                } catch (Exception rollbackEx) {
                    log.error("[SO-SERVICE] Failed to rollback adjustment for product {}:  {}",
                            adjustment.getProductId(), rollbackEx.getMessage());
                }
            }

            log.error("[SO-SERVICE] Failed to update item quantities:  {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Rollback stock reservations
     */
    private void rollbackReservations(List<OrderItem> items) {
        for (OrderItem item : items) {
            try {
                stockManagementService.releaseReservation(
                        item.getProduct().getProductId(),
                        item.getQuantity()
                );
                log.debug("[SO-SERVICE] Rolled back reservation for product:  {}", item.getProduct().getProductId());
            } catch (Exception e) {
                log.error("[SO-SERVICE] Failed to rollback reservation for product {}: {}",
                        item.getProduct().getProductId(), e.getMessage());
            }
        }
    }

    /**
     * Rollback S3 upload
     */
    private void rollbackS3Upload(String s3Key) {
        try {
            salesOrderQrCodeService.deleteQrCodeFromS3(s3Key);
            log.info("[SO-SERVICE] Successfully rolled back S3 upload:  {}", s3Key);
        } catch (Exception e) {
            log.error("[SO-SERVICE] Failed to rollback S3 upload {}: {}", s3Key, e.getMessage());
        }
    }

    /**
     * Generate unique order reference
     * Format: SO-yyyy-MM-dd-001
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderReference(LocalDate date) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String todayPrefix = "SO-" + date.format(dateFormatter) + "-";

            // Get latest order for today with pessimistic lock
            Optional<SalesOrder> lastOrderOpt = salesOrderRepository.findLatestSalesOrderWithPessimisticLock(
                    todayPrefix + "%");

            if (lastOrderOpt.isPresent()) {
                String lastOrderReference = lastOrderOpt.get().getOrderReference();

                // Validate format and extract number
                if (lastOrderReference.startsWith(todayPrefix)) {
                    String[] parts = lastOrderReference.split("-");

                    if (parts.length >= 5) {
                        try {
                            int lastOrderNumber = Integer.parseInt(parts[4]);
                            int nextOrderNumber = lastOrderNumber + 1;
                            String paddedNumber = String.format("%03d", nextOrderNumber);
                            return todayPrefix + paddedNumber;
                        } catch (NumberFormatException e) {
                            log.error("[SO-SERVICE] Invalid order number format: {}", lastOrderReference);
                        }
                    }
                }
            }

            // First order of the day
            return todayPrefix + "001";

        } catch (Exception e) {
            log.error("[SO-SERVICE] Error generating order reference: {}", e.getMessage());
            throw new ServiceException("Failed to generate unique order reference", e);
        }
    }
}