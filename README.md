# E-Commerce Platform — Microservices Architecture

> **Solution Architect Assignment**: High-Performance Product Service with Redis Caching, Kafka Events, Resilience4j, and Spring Data JPA

---

## 🏗️ Architecture Overview

```
                          ┌─────────────────────────────────┐
                          │         Client (Browser/App)     │
                          └──────────────┬──────────────────┘
                                         │ HTTP
                          ┌──────────────▼──────────────────┐
                          │         API Gateway              │
                          │   (Spring Cloud Gateway :8080)   │
                          │   • Rate Limiting (Redis)        │
                          │   • Circuit Breaker              │
                          │   • Load Balancing               │
                          └──────────────┬──────────────────┘
                                         │ lb://product-service
                          ┌──────────────▼──────────────────┐
                          │       Product Service            │
                          │          (:8081)                 │
                          │                                  │
                          │  ┌──────────────────────────┐   │
                          │  │   REST Controller         │   │
                          │  │   GET /products/{id}      │   │
                          │  └────────────┬─────────────┘   │
                          │               │ CompletableFuture│
                          │  ┌────────────▼─────────────┐   │
                          │  │    ProductService         │   │
                          │  │  @CircuitBreaker          │   │
                          │  │  @Retry @Bulkhead         │   │
                          │  └──┬──────────┬────────────┘   │
                          │     │          │                 │
                          │  ┌──▼──┐   ┌──▼──────────────┐  │
                          │  │Redis│   │  PostgreSQL/H2   │  │
                          │  │Cache│   │  (JPA Repository)│  │
                          │  │(10m)│   └─────────────────┘  │
                          │  └─────┘                         │
                          │                │                 │
                          │  ┌─────────────▼──────────────┐ │
                          │  │   Kafka Event Publisher     │ │
                          │  └─────────────────────────────┘ │
                          └────────────────┬────────────────┘
                                           │
                          ┌────────────────▼────────────────┐
                          │          Apache Kafka            │
                          │  Topics: product.created         │
                          │          product.updated         │
                          │          product.deleted         │
                          │          product.viewed          │
                          │          product.cache.          │
                          │          invalidation            │
                          └────────────────────────────────┘
```

## 📂 Project Structure

```
ecommerce-platform/
├── pom.xml                          # Multi-module root POM
├── docker-compose.yml               # Full stack local setup
│
├── discovery-server/                # Eureka Service Registry (:8761)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/ecommerce/discovery/
│       │   ├── DiscoveryServerApplication.java
│       │   └── SecurityConfig.java
│       └── resources/application.yml
│
├── api-gateway/                     # Spring Cloud Gateway (:8080)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/ecommerce/gateway/
│       │   ├── ApiGatewayApplication.java
│       │   └── FallbackController.java
│       └── resources/application.yml
│
└── product-service/                 # Core Product Microservice (:8081)
    ├── pom.xml
    └── src/
        ├── main/java/com/ecommerce/product/
        │   ├── ProductServiceApplication.java  # @EnableCaching @EnableAsync
        │   ├── config/
        │   │   ├── RedisConfig.java            # Cache config, TTLs, serialization
        │   │   ├── AsyncConfig.java            # ThreadPoolTaskExecutor
        │   │   └── KafkaConfig.java            # Producer/Consumer factories, Topics
        │   ├── controller/
        │   │   └── ProductController.java      # REST endpoints + async handling
        │   ├── service/
        │   │   └── ProductService.java         # Cache-Aside + Resilience4j
        │   ├── entity/
        │   │   └── Product.java                # JPA entity with @Embedded dimensions
        │   ├── repository/
        │   │   └── ProductRepository.java      # Custom JPA queries
        │   ├── dto/
        │   │   ├── ProductRequest.java         # Validation annotations
        │   │   ├── ProductResponse.java        # Serializable for Redis
        │   │   ├── ProductMapper.java          # MapStruct mapper
        │   │   └── ApiResponse.java            # Wrapper
        │   ├── event/
        │   │   ├── ProductEvent.java           # Event model
        │   │   ├── ProductEventPublisher.java  # Kafka producer
        │   │   └── ProductEventConsumer.java   # Kafka consumer (cache invalidation)
        │   └── exception/
        │       ├── ProductNotFoundException.java
        │       └── GlobalExceptionHandler.java
        └── resources/
            ├── application.yml                 # Full config (Redis, Kafka, R4J, JPA)
            ├── application-prod.yml            # Production overrides (PostgreSQL)
            └── data.sql                        # Seed data (10 sample products)
```

## 🔑 Key Design Decisions

### 1. Cache-Aside Pattern (Read-Aside)
```
Request → Check Redis (cache key: "product:{id}")
         ├── HIT  → Return cached, mark cacheStatus="HIT"
         └── MISS → Fetch DB → Store Redis (TTL=10min) → Return
```
- **Why Cache-Aside over Spring @Cacheable?** More control: manual TTL, cache status tagging, graceful Redis failure handling
- **Graceful degradation**: If Redis is down, service continues serving from DB (warn, don't fail)

### 2. Asynchronous Processing with CompletableFuture
```java
// ThreadPoolTaskExecutor: 10 core / 50 max / 1000 queue
@Async("productTaskExecutor")
public CompletableFuture<ProductResponse> getProductByIdAsync(Long id) { ... }
```
- Non-blocking request handling under high concurrent load
- Returns `CompletableFuture` from controller — Servlet container thread is released immediately
- `CallerRunsPolicy` prevents task rejection; applies backpressure instead

### 3. Resilience4j Layered Protection
```
Request → [RateLimiter: 100 req/s] → [CircuitBreaker: 50% fail threshold]
       → [Bulkhead: 50 concurrent calls] → [Retry: 3 attempts, exponential]
       → DB Call → [TimeLimiter: 3s timeout]
```

### 4. Distributed Cache Invalidation via Kafka
When a product is updated on any service instance:
```
Instance A: updates product → publishes "product.cache.invalidation" topic
Instance B: consumes event → evicts cache key locally
```
This prevents stale cache in horizontally scaled deployments.

### 5. Event-Driven Architecture
Every mutation publishes a Kafka event:
- `product.created` → downstream services (search index, recommendations)
- `product.updated` → cache invalidation, re-indexing
- `product.deleted` → cleanup downstream
- `product.viewed` → analytics, trending

---

## 🚀 Running the Application

### Option 1: Full Docker Stack (Recommended)
```bash
# Start all services (Kafka, Redis, PostgreSQL, all microservices, monitoring)
docker-compose up -d

# Check health
docker-compose ps

# View logs
docker-compose logs -f product-service
```

### Option 2: Local Development (H2 + local Redis/Kafka)
```bash
# Prerequisites: Redis on :6379, Kafka on :9092

# Start Discovery Server first
cd discovery-server
../mvnw spring-boot:run

# Start API Gateway
cd ../api-gateway
../mvnw spring-boot:run

# Start Product Service
cd ../product-service
../mvnw spring-boot:run
```

### Option 3: Product Service Standalone (no Eureka/Kafka needed)
```bash
cd product-service
../mvnw spring-boot:run -Dspring.cloud.discovery.enabled=false \
  -Dspring.kafka.producer.bootstrap-servers=localhost:9092
```

---

## 🌐 API Reference

### Base URL: `http://localhost:8080/api/v1/products`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/products/{id}` | Get product by ID (async, cached) |
| GET | `/products?category=Electronics&page=0&size=20` | List by category (paginated, cached) |
| GET | `/products/search?name=laptop` | Search products (async) |
| GET | `/products/categories` | All categories (1-hour cache) |
| POST | `/products` | Create product |
| PUT | `/products/{id}` | Update product + cache refresh |
| DELETE | `/products/{id}` | Soft delete + cache eviction |

### Example Request & Response

**GET** `/api/v1/products/1`

```json
{
  "success": true,
  "data": {
    "id": 1,
    "sku": "LAPTOP-001",
    "name": "ProBook 15 Laptop",
    "description": "High-performance laptop...",
    "price": 1299.99,
    "stockQuantity": 50,
    "category": "Electronics",
    "brand": "TechPro",
    "inStock": true,
    "cacheStatus": "HIT",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T11:00:00"
}
```

---

## 📊 Monitoring & Observability

| Service | URL | Credentials |
|---------|-----|-------------|
| Eureka Dashboard | http://localhost:8761 | eureka / eureka-secret |
| API Gateway | http://localhost:8080 | - |
| Product Service | http://localhost:8081 | - |
| H2 Console | http://localhost:8081/h2-console | sa / (empty) |
| Kafka UI | http://localhost:8090 | - |
| Redis Commander | http://localhost:8091 | - |
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3000 | admin / admin |

### Key Metrics
- `product.fetch.time` — time to retrieve a product
- `product.cache.hits` / `product.cache.misses` — cache effectiveness
- `product.cache.errors` — Redis failure rate
- `resilience4j.circuitbreaker.*` — circuit breaker state
- `http.server.requests` — request rates and latencies

---

## 🧪 Testing
```bash
# Unit tests only
mvn test -pl product-service

# Integration tests
mvn verify -pl product-service -Pintegration-test

# Build all (skip tests)
mvn clean package -DskipTests
```

---

## ⚙️ Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Framework | Spring Boot 3.2 | Core framework |
| Service Discovery | Spring Cloud Netflix Eureka | Dynamic service registration |
| API Gateway | Spring Cloud Gateway | Routing, rate limiting, CB |
| Caching | Spring Data Redis + Lettuce | Distributed cache (Read-Aside, 10-min TTL) |
| Database ORM | Spring Data JPA + Hibernate | Entity persistence |
| Database (Dev) | H2 (in-memory) | Fast local development |
| Database (Prod) | PostgreSQL 15 | Production persistence |
| Messaging | Apache Kafka | Async events + cache invalidation |
| Resilience | Resilience4j | CircuitBreaker, Retry, Bulkhead, RateLimiter |
| Async | Java CompletableFuture | Non-blocking request processing |
| DTO Mapping | MapStruct | Compile-time entity↔DTO mapping |
| Monitoring | Micrometer + Prometheus + Grafana | Observability |
| Containerization | Docker + Docker Compose | Local orchestration |
