# StreamCart

A microservices e-commerce platform for learning backend fundamentals.

## Architecture

```
Client (HTTP/WS)
       |
  [api-gateway]  :8080  — routing, rate limiting, JWT auth (Spring Cloud Gateway)
       |
  ┌────┴────────────────┐
  |                     |                      |
[user-service]  :8081   [product-service] :8082  [order-service] :8083
 JWT · REST · gRPC       REST · Redis cache       REST · Kafka producer
 PostgreSQL               MongoDB                  PostgreSQL
                               |                      |
                          — messaging layer —
                         [Kafka]          [RabbitMQ]
                      order-placed      notification queue
                                              |
                                   [notification-service] :8084
                                      WebSocket · email
```

## Services

| Service               | Port | DB         | Concepts                          |
|-----------------------|------|------------|-----------------------------------|
| api-gateway           | 8080 | —          | Reverse proxy, rate limit, JWT    |
| user-service          | 8081 | PostgreSQL | REST, gRPC, JWT, Security         |
| product-service       | 8082 | MongoDB    | REST, Redis cache, NoSQL          |
| order-service         | 8083 | PostgreSQL | REST, Kafka, ACID transactions    |
| notification-service  | 8084 | —          | RabbitMQ, WebSockets, Email       |

## Infrastructure (added later via Docker Compose)

- PostgreSQL
- MongoDB
- Redis
- Kafka + Zookeeper
- RabbitMQ
- Prometheus + Grafana
- Jenkins CI/CD

## Roadmap

1. ✅ Project skeleton (multi-module Maven, base packages)
2. ⬜ Docker Compose — spin up all infrastructure locally
3. ⬜ User Service — register/login, JWT, REST endpoints
4. ⬜ Product Service — CRUD, MongoDB, Redis caching
5. ⬜ Order Service — place order, PostgreSQL, Kafka producer
6. ⬜ Notification Service — RabbitMQ consumer, WebSocket push
7. ⬜ API Gateway — routing, JWT filter, rate limiting
8. ⬜ gRPC — User Service exposes gRPC, Order Service calls it
9. ⬜ Protobuf schemas
10. ⬜ Prometheus + Grafana metrics
11. ⬜ Jenkins CI/CD pipeline

we also need:
A global exception handler to map IllegalArgumentException to proper HTTP codes
A separate admin endpoint or seeding mechanism, since role is always USER
Account activation or email verification
Refresh tokens so users do not have to log in every 24 hours