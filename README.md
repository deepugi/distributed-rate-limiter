# Distributed Rate Limiter Service

A production-ready distributed rate limiting service built with Java and Spring Boot, backed by Redis. Supports Token Bucket and Sliding Window algorithms and handles concurrent requests across distributed nodes with full integration test coverage.

## Features

- Token Bucket algorithm for smooth, burst-tolerant rate limiting
- Sliding Window algorithm for strict per-interval limiting
- Redis-backed shared state across distributed nodes
- Thread-safe concurrent request handling
- Configurable limits per client/API key
- REST API for rate limit checks and status queries
- Full integration test suite with Testcontainers

## Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.x
- **Cache / State:** Redis (via Lettuce)
- **Testing:** JUnit 5, Mockito, Testcontainers
- **Build:** Maven
- **Containerization:** Docker, Docker Compose

## Project Structure

```
distributed-rate-limiter/
├── src/
│   ├── main/java/com/deepugi/ratelimiter/
│   │   ├── algorithm/
│   │   │   ├── TokenBucketLimiter.java
│   │   │   └── SlidingWindowLimiter.java
│   │   ├── config/
│   │   │   └── RedisConfig.java
│   │   ├── controller/
│   │   │   └── RateLimitController.java
│   │   ├── model/
│   │   │   └── RateLimitResponse.java
│   │   └── service/
│   │       └── RateLimiterService.java
│   └── test/java/com/deepugi/ratelimiter/
│       ├── TokenBucketLimiterTest.java
│       ├── SlidingWindowLimiterTest.java
│       └── RateLimitControllerIntegrationTest.java
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## Setup

### Prerequisites
- Java 17+
- Docker and Docker Compose

### Run locally

```bash
# Clone the repo
git clone https://github.com/deepugi/distributed-rate-limiter.git
cd distributed-rate-limiter

# Start Redis
docker-compose up -d redis

# Build and run
./mvnw spring-boot:run
```

### Run with Docker

```bash
docker-compose up --build
```

## API Usage

### Check rate limit

```http
POST /api/rate-limit/check
Content-Type: application/json

{
  "clientId": "user-123",
  "algorithm": "TOKEN_BUCKET",
  "maxRequests": 100,
  "windowSeconds": 60
}
```

**Response:**
```json
{
  "allowed": true,
  "clientId": "user-123",
  "remainingRequests": 99,
  "resetAfterSeconds": 58,
  "algorithm": "TOKEN_BUCKET"
}
```

### Get current status

```http
GET /api/rate-limit/status/{clientId}
```

## Algorithms

### Token Bucket
Allows a burst of requests up to the bucket capacity, then refills at a steady rate. Good for APIs that need to allow short bursts while enforcing an average rate.

### Sliding Window
Counts requests within a rolling time window. Stricter than token bucket — no burst allowance. Good for enforcing hard per-minute or per-hour limits.

## Running Tests

```bash
./mvnw test
```

Integration tests use Testcontainers to spin up a real Redis instance — no manual setup needed.

## License

MIT
