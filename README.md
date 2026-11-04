# Distributed Order System

A beginner-friendly distributed systems project using Java, Spring Boot, Kafka, Redis, Docker, and Kubernetes-ready service boundaries.

## Architecture (MVP)
- `order-service` — REST API to create an order and publish `OrderCreated` events to Kafka.
- `inventory-service` — consumes order events and simulates stock reservation.
- `notification-service` — consumes order events and simulates sending an order confirmation.
- Kafka — asynchronous communication between services.
- Redis — included in Docker Compose for the next learning phase (caching).
- MySQL — can be added in the persistence phase; this MVP keeps order data in memory so the event flow is easy to understand.

## Requirements
- Java 25+
- Maven 3.9+ (or use the included Maven Wrapper if you add one)
- Docker Desktop with Docker Compose

## Run
From the project root:

```bash
docker compose up --build
```

Then create an order:

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId":"LAPTOP-01","quantity":1,"customerEmail":"arun@example.com"}'
```

Check the order service:
```bash
curl http://localhost:8081/api/orders/health
```

Stop:
```bash
docker compose down
```

## Learning milestones
1. Run the three services and observe the Kafka event flow.
2. Add persistent storage for orders and inventory using MySQL/PostgreSQL.
3. Add Redis caching for product/inventory reads.
4. Add idempotency so duplicate Kafka events do not reserve stock twice.
5. Add retries and a dead-letter topic.
6. Add tests, GitHub Actions CI, metrics, and Kubernetes manifests.

## Important
This is a learning MVP, not production-ready. It uses a single Kafka broker and in-memory order state. Do not use real customer data or deploy it publicly without adding authentication, persistence, validation, and production-grade security.
