package com.sims.simscoreservice.qrCode.repository;


import com.sims.simscoreservice.qrCode.entity.SalesOrderQRCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Sales Order QR Code Repository
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Repository
public interface SalesOrderQrCodeRepository extends JpaRepository<SalesOrderQRCode, Long> {

    @Query("SELECT q FROM SalesOrderQRCode q WHERE q.qrToken = :token")
    Optional<SalesOrderQRCode> findByQrToken(@Param("token") String token);
}
