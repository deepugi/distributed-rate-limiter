package com.deepugi.ratelimiter.controller;

import com.deepugi.ratelimiter.model.RateLimitResponse;
import com.deepugi.ratelimiter.service.RateLimiterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rate-limit")
public class RateLimitController {

    private final RateLimiterService rateLimiterService;

    public RateLimitController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/check")
    public ResponseEntity<RateLimitResponse> checkRateLimit(@RequestBody Map<String, Object> request) {
        String clientId = (String) request.get("clientId");
        String algorithm = (String) request.getOrDefault("algorithm", "TOKEN_BUCKET");
        int maxRequests = (int) request.getOrDefault("maxRequests", 100);
        int windowSeconds = (int) request.getOrDefault("windowSeconds", 60);

        RateLimitResponse response = rateLimiterService.checkLimit(clientId, algorithm, maxRequests, windowSeconds);

        if (!response.isAllowed()) {
            return ResponseEntity.status(429).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{clientId}")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable String clientId) {
        Map<String, Object> status = rateLimiterService.getStatus(clientId);
        return ResponseEntity.ok(status);
    }
}
