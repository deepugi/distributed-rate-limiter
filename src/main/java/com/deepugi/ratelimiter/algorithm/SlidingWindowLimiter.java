package com.deepugi.ratelimiter.algorithm;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Sliding Window rate limiter backed by Redis sorted sets.
 * Counts requests within a rolling time window — no burst allowance.
 */
@Component
public class SlidingWindowLimiter {

    private final StringRedisTemplate redis;

    public SlidingWindowLimiter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * Attempt to allow a request within the sliding window.
     *
     * @param clientId      unique identifier for the client
     * @param maxRequests   maximum requests allowed in the window
     * @param windowSeconds window size in seconds
     * @return true if allowed, false if rate limited
     */
    public boolean tryConsume(String clientId, int maxRequests, int windowSeconds) {
        String key = "sw:" + clientId;
        long now = Instant.now().toEpochMilli();
        long windowStart = now - (windowSeconds * 1000L);

        // Remove timestamps outside the window
        redis.opsForZSet().removeRangeByScore(key, 0, windowStart);

        // Count requests in current window
        Long count = redis.opsForZSet().zCard(key);
        if (count != null && count >= maxRequests) {
            return false;
        }

        // Add current request timestamp
        redis.opsForZSet().add(key, String.valueOf(now), now);
        redis.expire(key, windowSeconds + 1, TimeUnit.SECONDS);

        return true;
    }

    public long getRequestCount(String clientId, int windowSeconds) {
        String key = "sw:" + clientId;
        long now = Instant.now().toEpochMilli();
        long windowStart = now - (windowSeconds * 1000L);
        redis.opsForZSet().removeRangeByScore(key, 0, windowStart);
        Long count = redis.opsForZSet().zCard(key);
        return count != null ? count : 0;
    }
}
