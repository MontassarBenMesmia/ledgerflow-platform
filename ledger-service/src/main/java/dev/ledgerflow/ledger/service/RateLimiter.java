package dev.ledgerflow.ledger.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RateLimiter {
    private final StringRedisTemplate redis;
    private final int limit;

    public RateLimiter(StringRedisTemplate redis, @Value("${ledger.rate-limit-per-minute:20}") int limit) {
        this.redis = redis;
        this.limit = limit;
    }

    public void check(String principal) {
        String key = "ledgerflow:rate:" + principal;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, Duration.ofMinutes(1));
        }
        if (count != null && count > limit) {
            throw new TransferService.LedgerException("Transfer rate limit exceeded");
        }
    }
}
