package com.deepugi.ratelimiter.model;

public class RateLimitResponse {

    private boolean allowed;
    private String clientId;
    private long remainingRequests;
    private long resetAfterSeconds;
    private String algorithm;

    public RateLimitResponse(boolean allowed, String clientId, long remainingRequests,
                              long resetAfterSeconds, String algorithm) {
        this.allowed = allowed;
        this.clientId = clientId;
        this.remainingRequests = remainingRequests;
        this.resetAfterSeconds = resetAfterSeconds;
        this.algorithm = algorithm;
    }

    public boolean isAllowed() { return allowed; }
    public String getClientId() { return clientId; }
    public long getRemainingRequests() { return remainingRequests; }
    public long getResetAfterSeconds() { return resetAfterSeconds; }
    public String getAlgorithm() { return algorithm; }
}
