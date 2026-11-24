package com.deepugi.ratelimiter;

import com.deepugi.ratelimiter.algorithm.TokenBucketLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenBucketLimiterTest {

    private StringRedisTemplate redis;
    private ValueOperations<String, String> valueOps;
    private TokenBucketLimiter limiter;

    @BeforeEach
    void setUp() {
        redis = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);
        limiter = new TokenBucketLimiter(redis);
    }

    @Test
    void allowsRequestWhenBucketFull() {
        when(valueOps.get(contains("tokens"))).thenReturn("10");
        when(valueOps.get(contains("timestamp"))).thenReturn(String.valueOf(System.currentTimeMillis() / 1000));

        boolean result = limiter.tryConsume("client-1", 10, 1.0);
        assertTrue(result);
    }

    @Test
    void deniesRequestWhenBucketEmpty() {
        when(valueOps.get(contains("tokens"))).thenReturn("0.0");
        when(valueOps.get(contains("timestamp"))).thenReturn(String.valueOf(System.currentTimeMillis() / 1000));

        boolean result = limiter.tryConsume("client-2", 10, 1.0);
        assertFalse(result);
    }

    @Test
    void allowsFirstRequestWithNoHistory() {
        when(valueOps.get(anyString())).thenReturn(null);

        boolean result = limiter.tryConsume("new-client", 5, 1.0);
        assertTrue(result);
    }
}
