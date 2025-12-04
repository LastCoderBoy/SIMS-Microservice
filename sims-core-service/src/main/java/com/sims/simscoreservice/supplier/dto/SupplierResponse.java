package com.sims.simscoreservice.supplier.dto;

import com.sims.simscoreservice.supplier.entity.Supplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Supplier Response DTO
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierResponse {
    private Long id;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor from entity
     */
    public SupplierResponse(Supplier supplier) {
        this.id = supplier.getId();
        this.name = supplier.getName();
        this.contactPerson = supplier.getContactPerson();
        this.email = supplier.getEmail();
        this.phone = supplier.getPhone();
        this.address = supplier.getAddress();
        this.createdAt = supplier.getCreatedAt();
        this.updatedAt = supplier.getUpdatedAt();
    }
}
