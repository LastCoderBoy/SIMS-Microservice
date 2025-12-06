package com.sims.simscoreservice.orderManagement.service.impl;

import com.sims.common.exceptions.*;
import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.confirmationToken.entity.ConfirmationToken;
import com.sims.simscoreservice.confirmationToken.service.ConfirmationTokenService;
import com.sims.simscoreservice.orderManagement.service.PurchaseOrderService;
import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.product.services.queryService.ProductQueryService;
import com.sims.simscoreservice.purchaseOrder.dto.DetailsPurchaseOrderView;
import com.sims.simscoreservice.purchaseOrder.dto.PurchaseOrderRequest;
import com.sims.simscoreservice.purchaseOrder.dto.SummaryPurchaseOrderView;
import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;
import com.sims.simscoreservice.purchaseOrder.queryService.PurchaseOrderQueryService;
import com.sims.simscoreservice.purchaseOrder.repository.PurchaseOrderRepository;
import com.sims.simscoreservice.purchaseOrder.strategy.PurchaseOrderSearchService;
import com.sims.simscoreservice.email.EmailService;
import com.sims.simscoreservice.admin.supplier.entity.Supplier;
import com.sims.simscoreservice.admin.supplier.service.SupplierService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Purchase Order Service Implementation
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private static final int MAX_PO_GENERATION_RETRIES = 5;

    private final Clock clock;
    private final PurchaseOrderSearchService purchaseOrderSearchService;
    private final PurchaseOrderQueryService purchaseOrderQueryService;
    private final SupplierService supplierService;
    private final ProductQueryService productQueryService;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailService emailService;

    // ============= Repository =============
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummaryPurchaseOrderView> getAllPurchaseOrders(int page, int size, String sortBy, String sortDirection) {
        return purchaseOrderQueryService.getAllPurchaseOrders(page, size, sortBy, sortDirection);
    }

    @Override
    @Transactional(readOnly = true)
    public DetailsPurchaseOrderView getDetailsForPurchaseOrder(Long orderId) {
        return purchaseOrderQueryService.getDetailsForPurchaseOrder(orderId);
    }

    @Override
    @Transactional
    public ApiResponse<PurchaseOrderRequest> createPurchaseOrder(@Valid PurchaseOrderRequest stockRequest, String username) {
        try {
            // Validate product is finalized
            Product orderedProduct = productQueryService.isProductFinalized(stockRequest.getProductId());

            // Create order entity
            PurchaseOrder order = createOrderEntity(stockRequest, orderedProduct, username);

            // Save and send email
            saveAndRequestPurchaseOrder(order);

            log.info("[PO-SERVICE] Product ordered successfully. PO Number: {}", order.getPoNumber());

            return ApiResponse.success("Order created successfully. PO Number: " + order.getPoNumber(), stockRequest);

        } catch (DataIntegrityViolationException de) {
            log.error("[PO-SERVICE] PO Number collision: {}", de.getMessage());
            throw new DatabaseException("Failed to create purchase order due to PO Number collision. Please try again.");
        } catch (ConstraintViolationException ve) {
            log.error("[PO-SERVICE] Invalid purchase order request: {}", ve.getMessage());
            throw new ValidationException("Invalid purchase order request: " + ve.getMessage());
        } catch (ResourceNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("[PO-SERVICE] Unexpected error creating purchase order: {}", e.getMessage(), e);
            throw new ServiceException("Failed to create purchase order", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummaryPurchaseOrderView> searchPurchaseOrders(String text, int page, int size, String sortBy, String sortDirection) {
        return purchaseOrderSearchService.searchAll(text, page, size, sortBy, sortDirection);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummaryPurchaseOrderView> filterPurchaseOrders(ProductCategories category, PurchaseOrderStatus status,
                                                                            String sortBy, String sortDirection, int page, int size) {
        return purchaseOrderSearchService.filterAll(category, status, sortBy, sortDirection, page, size);
    }

    /**
     * Create purchase order entity
     */
    private PurchaseOrder createOrderEntity(PurchaseOrderRequest stockRequest, Product orderedProduct, String orderedPerson) {
        Supplier supplier = supplierService.getSupplierEntityById(stockRequest.getSupplierId());
        String poNumber = generatePoNumber(supplier.getId());

        return new PurchaseOrder(
                orderedProduct,
                supplier,
                stockRequest.getOrderQuantity(),
                stockRequest.getExpectedArrivalDate(),
                stockRequest.getNotes(),
                poNumber,
                orderedPerson,
                clock
        );
    }

    /**
     * Save order and send email request to supplier
     */
    private void saveAndRequestPurchaseOrder(PurchaseOrder order) {
        purchaseOrderRepository.save(order);
        purchaseOrderRepository.flush();

        // Create confirmation token
        ConfirmationToken confirmationToken = confirmationTokenService.createConfirmationToken(order);

        // Send email to supplier
        emailService.sendPurchaseOrderRequest(order.getSupplier().getEmail(), order, confirmationToken);
    }

    /**
     * Generate unique PO number
     */
    private String generatePoNumber(Long supplierId) {
        try {
            for (int attempt = 0; attempt < MAX_PO_GENERATION_RETRIES; attempt++) {
                String uniqueIdPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                String potentialPONumber = "PO-" + supplierId + "-" + uniqueIdPart;

                if (! purchaseOrderRepository.existsByPoNumber(potentialPONumber)) {
                    return potentialPONumber;
                }

                log.warn("[PO-SERVICE] PO Number collision: {}.Retrying... (Attempt {}/{})",
                        potentialPONumber, attempt + 1, MAX_PO_GENERATION_RETRIES);
            }

            throw new ServiceException("Failed to generate unique PO Number after " + MAX_PO_GENERATION_RETRIES + " attempts");

        } catch (Exception e) {
            log.error("[PO-SERVICE] Failed to generate PO Number: {}", e.getMessage(), e);
            throw new ServiceException("Failed to generate unique PO Number", e);
        }
    }
}
