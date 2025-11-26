package com.sims.simscoreservice.product.mapper;

import com.sims.simscoreservice.product.dto.ProductRequest;
import com.sims.simscoreservice.product.dto.ProductResponse;
import com.sims.simscoreservice.product.entity.Product;
import org.mapstruct.*;

import java.util.List;

/**
 * Product Mapper (MapStruct)
 * Automatically generates conversion code between Entity and DTOs
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    /**
     * Convert Entity to Response DTO
     */
    ProductResponse toResponse(Product product);

    /**
     * Convert Entity list to Response DTO list
     */
    List<ProductResponse> toResponseList(List<Product> products);

    /**
     * Convert Request DTO to Entity (for create)
     */
    @Mapping(target = "productId", ignore = true)  // ID is generated
    @Mapping(target = "createdAt", ignore = true) // @CreationTimestamp
    @Mapping(target = "updatedAt", ignore = true) // @UpdateTimestamp
    Product toEntity(ProductRequest request);

    /**
     * Update existing entity from Request DTO (for update)
     * Only updates non-null fields from request
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "productId", ignore = true)  // Don't update ID
    @Mapping(target = "createdAt", ignore = true)  // Don't update creation time
    @Mapping(target = "updatedAt", ignore = true)  // Handled by @UpdateTimestamp
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product product);
}
