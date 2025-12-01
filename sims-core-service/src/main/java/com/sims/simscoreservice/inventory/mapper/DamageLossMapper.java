package com.sims.simscoreservice.inventory.mapper;

import com.sims.simscoreservice.inventory.dto.damageLoss.DamageLossResponse;
import com.sims.simscoreservice.inventory.entity.DamageLoss;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Damage/Loss Mapper (MapStruct)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DamageLossMapper {

    @Mapping(target = "productName", source = "inventory.product.name")
    @Mapping(target = "category", source = "inventory.product.category")
    @Mapping(target = "sku", source = "inventory.sku")
    DamageLossResponse toResponse(DamageLoss damageLoss);

    List<DamageLossResponse> toResponseList(List<DamageLoss> damageLosses);
}
