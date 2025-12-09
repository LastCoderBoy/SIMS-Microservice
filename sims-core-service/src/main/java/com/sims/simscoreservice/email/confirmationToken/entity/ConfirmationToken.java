package com.sims.simscoreservice.confirmationToken.entity;

import com.sims.simscoreservice.confirmationToken.enums.ConfirmationTokenStatus;
import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Confirmation Token Entity
 * Used for email confirmations (Purchase Orders)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Entity
@Table(name = "confirmation_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 64)
    private String token;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConfirmationTokenStatus status;


    // ========== Relationships ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder order;

    /**
     * Constructor for creating new token
     */
    public ConfirmationToken(String token, LocalDateTime createdAt, ConfirmationTokenStatus status,
                             LocalDateTime expiresAt, PurchaseOrder order) {
        this.token = token;
        this.createdAt = createdAt;
        this.status = status;
        this.expiresAt = expiresAt;
        this.order = order;
    }

    /**
     * Check if token is expired
     */
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Check if token has been used
     */
    public boolean isUsed() {
        return clickedAt != null;
    }
}