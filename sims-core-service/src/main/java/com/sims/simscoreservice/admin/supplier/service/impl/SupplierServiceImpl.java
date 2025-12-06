package com.sims.simscoreservice.admin.supplier.service.impl;

import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ResourceNotFoundException;
import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.ApiResponse;
import com.sims.simscoreservice.purchaseOrder.repository.PurchaseOrderRepository;
import com.sims.simscoreservice.admin.supplier.dto.SupplierRequest;
import com.sims.simscoreservice.admin.supplier.dto.SupplierResponse;
import com.sims.simscoreservice.admin.supplier.entity.Supplier;
import com.sims.simscoreservice.admin.supplier.mapper.SupplierMapper;
import com.sims.simscoreservice.admin.supplier.repository.SupplierRepository;
import com.sims.simscoreservice.admin.supplier.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Supplier Service Implementation
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierServiceImpl implements SupplierService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional
    public ApiResponse<SupplierResponse> createSupplier(SupplierRequest request) {
        try {
            // Check if supplier with same name already exists
            Optional<Supplier> existingByName = supplierRepository.findByName(request.getName());
            if (existingByName.isPresent()) {
                throw new ValidationException("Supplier with name '" + request.getName() + "' already exists");
            }

            // Check if supplier with same email already exists
            Optional<Supplier> existingByEmail = supplierRepository.findByEmail(request.getEmail());
            if (existingByEmail.isPresent()) {
                throw new ValidationException("Supplier with email '" + request.getEmail() + "' already exists");
            }

            // Create and save supplier
            Supplier supplier = supplierMapper.toEntity(request);
            Supplier savedSupplier = supplierRepository.save(supplier);
            supplierRepository.flush(); // Populate the timestamps

            log.info("[SUPPLIER-SERVICE] Supplier created: {}", savedSupplier.getName());

            SupplierResponse response = supplierMapper.toResponse(savedSupplier);
            return ApiResponse.success("Supplier created successfully!", response);

        } catch (ValidationException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("[SUPPLIER-SERVICE] Database error creating supplier: {}", e.getMessage());
            throw new DatabaseException("Failed to create supplier", e);
        } catch (Exception e) {
            log.error("[SUPPLIER-SERVICE] Error creating supplier: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create supplier", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        try {
            Supplier supplier = findSupplierById(id);
            return supplierMapper.toResponse(supplier);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("[SUPPLIER-SERVICE] Error getting supplier {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to get supplier", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Supplier getSupplierEntityById(Long id) {
        return findSupplierById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        try {
            List<Supplier> suppliers = supplierRepository.findAll();

            log.info("[SUPPLIER-SERVICE] Retrieved {} suppliers", suppliers.size());

            return supplierMapper.toResponseList(suppliers);

        } catch (DataAccessException e) {
            log.error("[SUPPLIER-SERVICE] Database error getting suppliers: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve suppliers", e);
        } catch (Exception e) {
            log.error("[SUPPLIER-SERVICE] Error getting suppliers: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve suppliers", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<SupplierResponse> updateSupplier(Long id, SupplierRequest request) {
        try {
            // Find existing supplier
            Supplier supplier = findSupplierById(id);

            // Check if new name conflicts with another supplier
            if (request.getName() != null && !request.getName().equalsIgnoreCase(supplier.getName())) {
                if (supplierRepository.existsByNameAndIdNot(request.getName(), id)) {
                    throw new ValidationException("Supplier with name '" + request.getName() + "' already exists");
                }
            }

            // Check if new email conflicts with another supplier
            if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(supplier.getEmail())) {
                if (supplierRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                    throw new ValidationException("Supplier with email '" + request.getEmail() + "' already exists");
                }
            }

            // Update supplier
            supplierMapper.updateEntityFromRequest(request, supplier);
            Supplier updatedSupplier = supplierRepository.save(supplier);

            log.info("[SUPPLIER-SERVICE] Supplier updated: {}", updatedSupplier.getName());

            SupplierResponse response = supplierMapper.toResponse(updatedSupplier);
            return ApiResponse.success(supplier.getName() + " updated successfully.", response);

        } catch (ResourceNotFoundException | ValidationException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("[SUPPLIER-SERVICE] Database error updating supplier: {}", e.getMessage());
            throw new DatabaseException("Failed to update supplier", e);
        } catch (Exception e) {
            log.error("[SUPPLIER-SERVICE] Error updating supplier {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update supplier", e);
        }
    }

    @Override
    @Transactional
    public void deleteSupplier(Long supplierId) {
        try {
            // Check if supplier exists
            if (!supplierRepository.existsById(supplierId)) {
                throw new ResourceNotFoundException("Supplier not found with ID: " + supplierId);
            }

            // Check if supplier has active purchase orders
            if (purchaseOrderRepository.existsActivePurchaseOrdersForSupplier(supplierId)) {
                throw new ValidationException(
                        "Cannot delete supplier. Active purchase orders exist.  " +
                                "Please complete or cancel all orders before deleting the supplier."
                );
            }

            supplierRepository.deleteById(supplierId);

            log.info("[SUPPLIER-SERVICE] Supplier deleted: {}", supplierId);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("[SUPPLIER-SERVICE] Database error deleting supplier: {}", e.getMessage());
            throw new DatabaseException("Failed to delete supplier", e);
        } catch (Exception e) {
            log.error("[SUPPLIER-SERVICE] Error deleting supplier {}: {}", supplierId, e.getMessage());
            throw new RuntimeException("Failed to delete supplier", e);
        }
    }

    /**
     * Find supplier by ID or throw exception
     */
    private Supplier findSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));
    }
}
