package com.sims.simscoreservice.confirmationToken.scheduler;

import com.sims.simscoreservice.confirmationToken.service.ConfirmationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Confirmation Token Scheduler
 * Automatically expires old tokens every 12 hours
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConfirmationTokenScheduler {

    private final ConfirmationTokenService tokenService;

    // @Scheduled(cron = "*/15 * * * * *")  //15 seconds
    @Scheduled(cron = "0 0 */12 * * *")
    public void expireOldTokens() {
        log.info("[TOKEN-SCHEDULER] Checking for expired confirmation tokens...");
        tokenService.expireTokens();
    }
}
