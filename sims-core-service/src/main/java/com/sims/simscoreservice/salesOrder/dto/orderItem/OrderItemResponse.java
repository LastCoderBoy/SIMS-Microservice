package com.sims.simscoreservice.salesOrder.dto.orderItem;

import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.salesOrder.enums.OrderItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Order Item Response DTO
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    private Long id;
    private String productId;
    private String productName;
    private ProductCategories productCategory;
    private OrderItemStatus orderItemStatus;
    private Integer quantity;
    private Integer approvedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
