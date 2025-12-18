package com.sims.simscoreservice.supplier.mapper;

import com.sims.simscoreservice.supplier.dto.SupplierRequest;
import com.sims.simscoreservice.supplier.dto.SupplierResponse;
import com.sims.simscoreservice.supplier.entity.Supplier;
import org.mapstruct. Mapper;
import org.mapstruct.MappingTarget;
import org. mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Supplier Mapper (MapStruct)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SupplierMapper {

    /**
     * Convert request DTO to entity
     */
    Supplier toEntity(SupplierRequest request);

    /**
     * Convert entity to response DTO
     */
    SupplierResponse toResponse(Supplier supplier);

    /**
     * Convert list of entities to list of response DTOs
     */
    List<SupplierResponse> toResponseList(List<Supplier> suppliers);

    /**
     * Update entity from request (ignores null values)
     */
    void updateEntityFromRequest(SupplierRequest request, @MappingTarget Supplier supplier);
}
