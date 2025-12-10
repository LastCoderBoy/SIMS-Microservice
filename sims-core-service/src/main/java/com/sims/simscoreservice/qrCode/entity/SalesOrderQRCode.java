package com.sims.simscoreservice.qrCode.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Sales Order QR Code Entity
 * Stores QR code information for order tracking
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Entity
@Table(name = "sales_order_qr_codes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderQRCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "qr_token", unique = true, nullable = false, length = 100)
    private String qrToken;

    @Column(name = "qr_code_s3_key", nullable = false, length = 500)
    private String qrCodeS3Key;

    @CreationTimestamp
    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "last_scanned_at")
    private LocalDateTime lastScannedAt;

    // ***** Scanner details *****
    @Column(name = "scanned_by", length = 100)
    private String scannedBy; // username of the scanner (linked to SIMS user)

    @Column(name = "ip_address", length = 45)
    private String ipAddress; // last scanner IP

    @Column(name = "user_agent", length = 255)
    private String userAgent; // "Chrome on Windows" etc.

    // ***** Relationship detail *****
    @OneToOne(mappedBy = "qrCode")
    @JsonIgnore  // Avoid infinite loop in JSON serialization
    private SalesOrder salesOrder;
}
