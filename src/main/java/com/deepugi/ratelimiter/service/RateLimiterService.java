package com.deepugi.ratelimiter.service;

import com.deepugi.ratelimiter.algorithm.SlidingWindowLimiter;
import com.deepugi.ratelimiter.algorithm.TokenBucketLimiter;
import com.deepugi.ratelimiter.model.RateLimitResponse;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RateLimiterService {

    private final TokenBucketLimiter tokenBucketLimiter;
    private final SlidingWindowLimiter slidingWindowLimiter;

    public RateLimiterService(TokenBucketLimiter tokenBucketLimiter,
                               SlidingWindowLimiter slidingWindowLimiter) {
        this.tokenBucketLimiter = tokenBucketLimiter;
        this.slidingWindowLimiter = slidingWindowLimiter;
    }

    public RateLimitResponse checkLimit(String clientId, String algorithm,
                                         int maxRequests, int windowSeconds) {
        boolean allowed;
        long remaining;

        if ("SLIDING_WINDOW".equalsIgnoreCase(algorithm)) {
            allowed = slidingWindowLimiter.tryConsume(clientId, maxRequests, windowSeconds);
            long used = slidingWindowLimiter.getRequestCount(clientId, windowSeconds);
            remaining = Math.max(0, maxRequests - used);
        } else {
            double refillRate = (double) maxRequests / windowSeconds;
            allowed = tokenBucketLimiter.tryConsume(clientId, maxRequests, refillRate);
            remaining = (long) tokenBucketLimiter.getRemainingTokens(clientId, maxRequests);
        }

        return new RateLimitResponse(allowed, clientId, remaining, windowSeconds, algorithm.toUpperCase());
    }

    public Map<String, Object> getStatus(String clientId) {
        return Map.of(
            "clientId", clientId,
            "tokenBucketTokens", tokenBucketLimiter.getRemainingTokens(clientId, 100),
            "slidingWindowCount", slidingWindowLimiter.getRequestCount(clientId, 60)
        );
    }
}
