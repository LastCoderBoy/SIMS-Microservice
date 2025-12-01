package com.sims.simscoreservice.inventory.entity;

import com.sims.simscoreservice.inventory.enums.LossReason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate. annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * Damage/Loss Entity
 * Tracks damaged or lost inventory items
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Entity
@Table(name = "damage_losses")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DamageLoss {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku", nullable = false)
    private Inventory inventory;

    @Column(name = "quantity_lost", nullable = false)
    private Integer quantityLost;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 30)
    private LossReason reason;

    @Column(name = "loss_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal lossValue;

    @Column(name = "loss_date", nullable = false)
    private LocalDateTime lossDate;

    @Column(name = "recorded_by", length = 100)
    private String recordedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
