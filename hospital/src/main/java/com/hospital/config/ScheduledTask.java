// ScheduledTask.java
package com.hospital.config;

import com.hospital.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTask {

    private final EmailService emailService;

    /**
     * 每小时清理过期验证码
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanExpiredVerificationCodes() {
        log.info("开始清理过期验证码...");
        emailService.cleanExpiredCodes();
    }
}
