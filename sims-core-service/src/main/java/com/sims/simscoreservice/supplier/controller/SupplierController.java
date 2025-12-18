package com.sims.simscoreservice.supplier.controller;

import com.sims.common.models.ApiResponse;
import com.sims.simscoreservice.shared.util.RoleValidator;
import com.sims.simscoreservice.supplier.dto.SupplierRequest;
import com.sims.simscoreservice.supplier.dto.SupplierResponse;
import com.sims.simscoreservice.supplier.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sims.common.constants.AppConstants.*;

/**
 * Supplier Controller
 * Manages supplier CRUD operations
 * Only ADMIN can manage suppliers
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RestController
@RequestMapping(BASE_SUPPLIERS_PATH)
@RequiredArgsConstructor
@Slf4j
public class SupplierController {

    private final SupplierService supplierService;
    private final RoleValidator roleValidator;

    /**
     * Create new supplier
     * Only ADMIN can create
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(
            @Valid @RequestBody SupplierRequest request,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        log.info("[SUPPLIER-CONTROLLER] Create supplier by user: {}", userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN"); // might throw Forbidden Exception

        ApiResponse<SupplierResponse> response = supplierService.createSupplier(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplierById(
            @PathVariable Long id,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[SUPPLIER-CONTROLLER] Get supplier {} by user: {}", id, userId);

        SupplierResponse response = supplierService.getSupplierById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all suppliers
     */
    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers(
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[SUPPLIER-CONTROLLER] Get all suppliers by user: {}", userId);

        List<SupplierResponse> suppliers = supplierService.getAllSuppliers();

        return ResponseEntity.ok(suppliers);
    }

    /**
     * Update supplier
     * Only ADMIN can update
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(
            @PathVariable Long id,
            @RequestBody SupplierRequest request,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        log.info("[SUPPLIER-CONTROLLER] Update supplier {} by user: {}", id, userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN"); // might throw Forbidden Exception

        ApiResponse<SupplierResponse> response = supplierService.updateSupplier(id, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete supplier
     * Only ADMIN can delete
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(
            @PathVariable Long id,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        log.info("[SUPPLIER-CONTROLLER] Delete supplier {} by user: {}", id, userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN"); // might throw Forbidden Exception

        supplierService.deleteSupplier(id);

        return ResponseEntity.noContent().build();
    }
}
