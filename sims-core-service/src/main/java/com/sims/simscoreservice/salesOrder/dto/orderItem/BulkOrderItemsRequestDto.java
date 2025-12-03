package com.sims.simscoreservice.salesOrder.dto.orderItem;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bulk Order Items Request DTO
 * Used for creating orders with multiple items
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkOrderItemsRequestDto {

    @NotEmpty(message = "Order items are required")
    @Valid
    private List<OrderItemRequest> orderItems;
}
