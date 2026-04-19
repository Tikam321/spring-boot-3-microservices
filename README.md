# Spring Boot Microservices

An event-driven microservices architecture for e-commerce with 5 independent services communicating via Apache Kafka.

## Architecture

```
Client → API Gateway (9000) → Product Service (8080)
                          → Order Service (8084)
                          → Inventory Service (8082)
                          → Notification Service (8086)
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| api-gateway | 9000 | Entry point, routing, circuit breaker |
| product-service | 8080 | Product CRUD (MongoDB) |
| order-service | 8084 | Order placement, publishes Kafka events |
| inventory-service | 8082 | Stock management with distributed locking |
| notification-service | 8086 | Email notifications via Kafka listeners |

## Features

### Event-Driven Architecture
- Apache Kafka for async communication between services
- Topics: `order-placed`, `inventory-reserved`, `order-confirmed`

### Resilience Patterns
- **Circuit Breaker**: Resilience4j on API Gateway
- **Distributed Locking**: Redisson for inventory operations
- **Retry Mechanism**: Configured via Resilience4j

### API Gateway
- Spring Cloud Gateway with route configuration
- Circuit breakers for each service
- Swagger UI via SpringDoc OpenAPI
- Fallback routes for service unavailability

### Observability
- Spring Boot Actuator with health checks
- Prometheus metrics
- Distributed tracing (Zipkin/Brave)
- Loki logging integration

### Database
- MongoDB (product-service)
- PostgreSQL (order-service, inventory-service)

### Security
- Spring Security configured
- OAuth2 JWT ready (Keycloak integration)
- CORS enabled

## Configuration

### Service URLs
```
product.service.url=http://localhost:8080
order.service.url=http://localhost:8084
inventory.service.url=http://localhost:8082
```

### Circuit Breaker Settings
- Window: COUNT_BASED (10 calls)
- Failure rate threshold: 50%
- Wait duration: 5s

## Tech Stack

- Java 17
- Spring Boot 3.5.x
- Spring Cloud Gateway
- Apache Kafka
- Resilience4j
- Redisson
- PostgreSQL
- MongoDB
- Prometheus + Grafana
- Docker + Kubernetes

## Running

```bash
# Build all services
mvn clean package

# Run with Docker Compose
docker-compose up
```

## API Endpoints

- `POST /api/product` - Create product
- `GET /api/product` - Get all products
- `POST /api/order` - Place order
- `GET /api/inventory?skuCode=X&quantity=Y` - Check stock
- `POST /api/inventory` - Add inventory