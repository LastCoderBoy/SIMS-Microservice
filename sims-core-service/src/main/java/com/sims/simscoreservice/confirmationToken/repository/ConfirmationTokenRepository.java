package com.sims.simscoreservice.confirmationToken.repository;

import com.sims.simscoreservice.confirmationToken.entity.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Confirmation Token Repository
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {

    /**
     * Find token by token string
     */
    Optional<ConfirmationToken> findByToken(String token);

    /**
     * Find all expired tokens that haven't been clicked
     */
    @Query("SELECT ct FROM ConfirmationToken ct WHERE ct.expiresAt < :now AND ct.clickedAt IS NULL")
    List<ConfirmationToken> findAllExpiredAndUnused(@Param("now") LocalDateTime now);
}
