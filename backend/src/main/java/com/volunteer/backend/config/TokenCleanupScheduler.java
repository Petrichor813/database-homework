package com.volunteer.backend.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.volunteer.backend.repository.TokenRepository;

@Component
public class TokenCleanupScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupScheduler.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TokenRepository tokenRepository;

    public TokenCleanupScheduler(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    // 第一个 0 表示秒数（0 ~ 59）
    // 第二个 0 表示分钟（0 ~ 59）
    // 第三个 2 表示小时（0 ~ 23）
    // 第四个 * 表示日期
    // 第五个 * 表示月份
    // 第六个 ? 表示星期（0 ~ 6 或者 MON ~ SAT）不指定
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("开始清理过期Token，当前时间: {}", now.format(formatter));

        try {
            tokenRepository.deleteExpiredTokens(now);
            logger.info("过期Token清理完成");
        } catch (Exception e) {
            logger.error("清理过期Token时发生错误", e);
        }
    }
}
