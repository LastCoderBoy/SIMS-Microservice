package com.sims.simscoreservice.salesOrder.dto;

import com.sims.simscoreservice.salesOrder.dto.orderItem.OrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Sales Order Request DTO
 * Used for creating new sales orders
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderRequest {

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotEmpty(message = "Order items are required")
    @Valid
    private List<OrderItemRequest> orderItems;
}