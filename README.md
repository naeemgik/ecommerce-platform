# Enterprise Ecommerce Platform

Standalone Spring Boot ecommerce platform showing a service-owned database model, Redis read-through caching, and Kafka-based saga choreography for order processing.

## Architecture

Requests enter through clients or the optional API gateway, then flow into small bounded-context services. Each business service owns its H2 datastore. Kafka carries the checkout workflow between services, while Redis is used for high-volume product cache reads.

See the layered diagram in `docs/ecommerce-platform-architecture.drawio`.

## Services

### Service Catalogue

- `api-gateway`: Spring Cloud Gateway entry point for external traffic. It handles route forwarding, request rate limiting, circuit breaker fallback, and service lookup through Eureka.
- `discovery-server`: Eureka registry used by gateway and services for service discovery. It runs with basic authentication so services can register and resolve each other consistently.
- `product-service`: Owns product catalog reads backed by H2. It uses Redis read-through caching and async responses to reduce repeated database hits for hot products.
- `cart-service`: Owns customer cart operations such as add item, read cart, and clear cart. It calls `product-service` to enrich cart items with product details.
- `order-service`: Owns order creation, product validation, and order status projection. It starts the saga by publishing `order.created` and updates orders from downstream Kafka events.
- `inventory-service`: Reserves stock when new orders arrive. It publishes success or rejection events so the saga can continue or cancel cleanly.
- `payment-service`: Authorizes payment after inventory has been reserved. It records payment attempts and publishes processed or failed payment events.
- `fulfillment-service`: Confirms warehouse fulfillment after successful payment. It records fulfillment state and publishes confirmation for delivery scheduling.
- `delivery-service`: Schedules delivery after fulfillment confirmation. It stores delivery records and publishes `delivery.scheduled`.
- `notification-service`: Records customer-facing business events from the saga. It is the place to plug in email, SMS, or push notification fan-out.
- `common-events`: Shared Maven module for Kafka topic names, event payloads, and status enums. It keeps service contracts consistent across the platform.

### Ports And Integrations

| Service | Port | Main responsibility | Storage / integration |
| --- | ---: | --- | --- |
| `api-gateway` | 8080 | Gateway routing, rate limiting, circuit breaker fallback | Redis rate limiter, Eureka discovery |
| `discovery-server` | 8761 | Eureka service registry | Basic auth: `eureka` / `eureka-secret` |
| `product-service` | 8081 | Product catalog API and async product lookup | H2 `productdb`, Redis key `product:{id}` |
| `inventory-service` | 8082 | Stock reservation after order creation | H2 `inventorydb`, Kafka consumer/producer |
| `payment-service` | 8083 | Payment authorization after inventory reservation | H2 `paymentdb`, Kafka consumer/producer |
| `order-service` | 8084 | Order API, product validation, saga state projection | H2 `orderdb`, Kafka consumer/producer |
| `notification-service` | 8085 | Notification log for customer-facing events | H2 `notificationdb`, Kafka consumer |
| `cart-service` | 8086 | Customer cart CRUD with product enrichment | H2 `cartdb`, calls `product-service` |
| `fulfillment-service` | 8087 | Warehouse fulfillment confirmation | H2 `fulfillmentdb`, Kafka consumer/producer |
| `delivery-service` | 8088 | Delivery scheduling after fulfillment | H2 `deliverydb`, Kafka consumer/producer |
| `common-events` | - | Shared topic names, events, and status enums | Maven library module |

## Order Saga

1. `order-service` validates products through `product-service` using `WebClient`, `CompletableFuture`, and `resilience4j`.
2. `order-service` stores the order and publishes `order.created`.
3. `inventory-service` reserves stock and publishes `inventory.reserved` or `inventory.rejected`.
4. `payment-service` consumes reserved inventory and publishes `payment.processed` or `payment.failed`.
5. `fulfillment-service` consumes successful payments and publishes `fulfillment.confirmed`.
6. `delivery-service` consumes fulfillment confirmation and publishes `delivery.scheduled`.
7. `order-service` listens to downstream events to update the aggregate status.
8. `notification-service` records key business events for email, SMS, or push fan-out.

Failure paths publish `order.cancelled` so downstream services can release resources or trigger support workflows.

## Key Endpoints

| Endpoint | Service | Purpose |
| --- | --- | --- |
| `GET /api/products/{productId}` | `product-service` | Fetch product details through async Redis read-through cache |
| `GET /api/carts/{customerId}/items` | `cart-service` | Read customer cart |
| `POST /api/carts/{customerId}/items` | `cart-service` | Add product to cart |
| `DELETE /api/carts/{customerId}/items` | `cart-service` | Clear cart |
| `POST /api/orders` | `order-service` | Create order and start saga |
| `GET /api/orders/{orderId}` | `order-service` | Read order and current saga status |
| `GET /api/inventory` | `inventory-service` | Inspect seeded inventory |
| `GET /api/payments` | `payment-service` | Inspect payment records |
| `GET /api/fulfillments` | `fulfillment-service` | Inspect fulfillment records |
| `GET /api/deliveries` | `delivery-service` | Inspect delivery records |
| `GET /api/notifications` | `notification-service` | Inspect notification logs |

## Product Caching

`product-service` uses a Redis read-through pattern:

1. Read `product:{id}` from Redis.
2. On miss, query H2 through Spring Data JPA.
3. Cache the product for 10 minutes.
4. Return the result asynchronously with `CompletableFuture`.

## Resilience And Scale

- Services can scale independently because each service owns its datastore and Kafka consumer group.
- Kafka buffers checkout spikes and decouples slow workflow steps.
- Redis reduces repeated product database reads.
- `resilience4j` circuit breaker, retry, and time limiter protect synchronous product validation.
- `common-events` keeps topic names, event payloads, and status enums consistent.

## Run Locally

Start Kafka and Redis:

```bash
docker compose up -d
```

Compile the parent Maven modules:

```bash
mvn -q -DskipTests compile
```

Run services in separate terminals:

```bash
cd discovery-server && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd inventory-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd fulfillment-service && mvn spring-boot:run
cd delivery-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd cart-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
```

Create an order:

```bash
curl -X POST http://localhost:8084/api/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": 42,
    "items": [
      { "productId": 1, "quantity": 1 },
      { "productId": 2, "quantity": 2 }
    ]
  }'
```

Check the order status:

```bash
curl http://localhost:8084/api/orders/1
```

Stop infrastructure:

```bash
docker compose down
```
