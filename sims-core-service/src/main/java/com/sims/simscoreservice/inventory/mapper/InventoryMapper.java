package com.sims.simscoreservice.inventory.mapper;

import com.sims.simscoreservice.inventory.dto.InventoryResponse;
import com.sims.simscoreservice.inventory.entity.Inventory;
import org.mapstruct.*;

import java.util.List;

/**
 * Inventory Mapper (MapStruct)
 * Automatically generates conversion code between Entity and DTOs
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy. IGNORE)
public interface InventoryMapper {

    /**
     * Convert Inventory Entity to Response DTO
     */
    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "category", source = "product.category")
    @Mapping(target = "price", source = "product.price")
    @Mapping(target = "productStatus", source = "product.status")
    @Mapping(target = "inventoryStatus", source = "status")
    @Mapping(target = "availableStock", expression = "java(inventory.getAvailableStock())")
    @Mapping(target = "lastUpdate", expression = "java(inventory.getLastUpdate() != null ? inventory.getLastUpdate().toString() : null)")
    InventoryResponse toResponse(Inventory inventory);

    /**
     * Convert list of Inventory entities to Response DTOs
     */
    List<InventoryResponse> toResponseList(List<Inventory> inventories);
}
