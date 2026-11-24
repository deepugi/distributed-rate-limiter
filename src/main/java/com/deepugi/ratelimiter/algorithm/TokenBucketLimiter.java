package com.deepugi.ratelimiter.algorithm;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Token Bucket rate limiter backed by Redis.
 * Allows bursts up to maxTokens, refills at a steady rate.
 */
@Component
public class TokenBucketLimiter {

    private final StringRedisTemplate redis;

    public TokenBucketLimiter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * Attempt to consume one token for the given client.
     *
     * @param clientId     unique identifier for the client
     * @param maxTokens    maximum tokens (bucket capacity)
     * @param refillRate   tokens added per second
     * @return true if the request is allowed, false if rate limited
     */
    public boolean tryConsume(String clientId, int maxTokens, double refillRate) {
        String tokensKey = "tb:tokens:" + clientId;
        String timestampKey = "tb:timestamp:" + clientId;

        long now = Instant.now().getEpochSecond();

        String storedTokens = redis.opsForValue().get(tokensKey);
        String storedTimestamp = redis.opsForValue().get(timestampKey);

        double tokens = storedTokens != null ? Double.parseDouble(storedTokens) : maxTokens;
        long lastRefill = storedTimestamp != null ? Long.parseLong(storedTimestamp) : now;

        // Refill tokens based on elapsed time
        long elapsed = now - lastRefill;
        tokens = Math.min(maxTokens, tokens + elapsed * refillRate);

        if (tokens < 1.0) {
            return false;
        }

        tokens -= 1.0;

        redis.opsForValue().set(tokensKey, String.valueOf(tokens), 3600, TimeUnit.SECONDS);
        redis.opsForValue().set(timestampKey, String.valueOf(now), 3600, TimeUnit.SECONDS);

        return true;
    }

    public double getRemainingTokens(String clientId, int maxTokens) {
        String val = redis.opsForValue().get("tb:tokens:" + clientId);
        return val != null ? Double.parseDouble(val) : maxTokens;
    }
}
