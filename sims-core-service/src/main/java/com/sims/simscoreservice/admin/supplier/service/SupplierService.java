package com.sims.simscoreservice.admin.supplier.service;

import com.sims.common.models.ApiResponse;
import com.sims.simscoreservice.admin.supplier.dto.SupplierRequest;
import com.sims.simscoreservice.admin.supplier.dto.SupplierResponse;
import com.sims.simscoreservice.admin.supplier.entity.Supplier;

import java. util.List;

/**
 * Supplier Service Interface
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface SupplierService {

    ApiResponse<SupplierResponse> createSupplier(SupplierRequest request);
    SupplierResponse getSupplierById(Long id);
    Supplier getSupplierEntityById(Long id);
    List<SupplierResponse> getAllSuppliers();
    ApiResponse<SupplierResponse> updateSupplier(Long id, SupplierRequest request);
    void deleteSupplier(Long id);
}
