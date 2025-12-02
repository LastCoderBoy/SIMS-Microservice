package com.sims.simscoreservice.stockMovement.entity;


import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.stockMovement.enums.StockMovementReferenceType;
import com.sims.simscoreservice.stockMovement.enums.StockMovementType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockMovementType type; // IN, OUT

    @Column(name = "reference_id", nullable = false)
    private String referenceId; // SO or PO ID

    @Column(name = "reference_type", nullable = false)
    private StockMovementReferenceType referenceType; // SALES_ORDER, PURCHASE_ORDER

    @Column(nullable = false, name = "created_by")
    private String createdBy;

    @CreationTimestamp
    @Column(updatable = false, nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    public StockMovement(Product product, Integer quantity, StockMovementType type,
                         String referenceId, StockMovementReferenceType referenceType, String createdBy) {
        this.product = product;
        this.quantity = quantity;
        this.type = type;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.createdBy = createdBy;
    }
}
