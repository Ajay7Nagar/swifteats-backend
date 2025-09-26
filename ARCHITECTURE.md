# SwiftEats Backend Architecture

## Table of Contents
1. [Problem Statement and Context](#problem-statement-and-context)
2. [Architectural Pattern Selection](#architectural-pattern-selection)
3. [System Architecture Overview](#system-architecture-overview)
4. [Component Responsibilities](#component-responsibilities)
5. [Data Flow Descriptions](#data-flow-descriptions)
6. [Technology Stack Justification](#technology-stack-justification)
7. [Performance Targets and Optimization](#performance-targets-and-optimization)
8. [Resilience and Failure Handling](#resilience-and-failure-handling)
9. [Local Validation and Testing](#local-validation-and-testing)

---

## Problem Statement and Context

### Business Challenge
SwiftEats is a food delivery startup launching in Maharashtra that requires a backend platform capable of:
- Processing 500 orders per minute during peak hours
- Delivering menu browsing experience with P99 response times under 200ms
- Handling real-time GPS tracking from 10,000 concurrent drivers (2,000 events/second)
- Maintaining 99.9% uptime while supporting rapid feature development

### Technical Constraints
- **Local Validation**: System must demonstrate full functionality on local development machines
- **Deployment**: Single docker-compose file for easy validation and deployment
- **Resource Efficiency**: Optimal performance within reasonable hardware constraints
- **Development Speed**: Support for rapid iteration and feature development

### Design Principles
- **Avoid Over-Engineering**: Choose simplest solution that meets requirements
- **Cost Efficiency**: Select proven, cost-effective technologies over premium alternatives
- **Reliability First**: Prioritize system stability and data consistency
- **Future-Ready**: Design module boundaries for potential future service extraction

---

## Architectural Pattern Selection

### Chosen Pattern: Modular Monolith

**Rationale:**
- **Startup Alignment**: Enables rapid development and deployment for product-market fit validation
- **Performance**: In-process communication eliminates network latency between components
- **Simplicity**: Single deployment unit reduces operational complexity
- **Cost Effectiveness**: Minimal infrastructure requirements for target scale
- **Local Development**: Single container setup with standard development hardware requirements

**Trade-offs Accepted:**
- Component scaling granularity limited to application level
- Technology stack uniformity across all modules
- Shared failure domain requiring robust error handling

---

## System Architecture Overview

```
                                SwiftEats Modular Monolith Architecture
    
    ┌─────────────────────────────────────────────────────────────────────────────────────────┐
    │                                   Load Balancer                                         │
    │                                 (NGINX/HAProxy)                                         │
    └────────────────────────────┬────────────────────────────────────────────────────────────┘
                                │
    ┌───────────────────────────▼─────────────────────────────────────────────────────────────┐
    │                            SwiftEats Application                                        │
    │  ┌─────────────────────────────────────────────────────────────────────────────────┐   │
    │  │                           API Gateway Layer                                     │   │
    │  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐  │   │
    │  │  │   Order     │ │ Restaurant  │ │  Location   │ │    User     │ │   Driver    │  │   │
    │  │  │  Endpoints  │ │  Endpoints  │ │  Endpoints  │ │  Endpoints  │ │  Endpoints  │  │   │
    │  │  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘  │   │
    │  └─────────────────────────────────────────────────────────────────────────────────┘   │
    │                                         │                                               │
    │  ┌─────────────────────────────────────▼───────────────────────────────────────────┐   │
    │  │                          Business Logic Layer                                   │   │
    │  │                                                                                  │   │
    │  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐  │   │
    │  │  │   Order     │ │ Restaurant  │ │  Location   │ │    User     │ │  Notification│  │   │
    │  │  │   Module    │ │   Module    │ │   Module    │ │   Module    │ │   Module    │  │   │
    │  │  │             │ │             │ │             │ │             │ │             │  │   │
    │  │  │ - Order     │ │ - Menu      │ │ - GPS       │ │ - Customer  │ │ - WebSocket │  │   │
    │  │  │   Creation  │ │   Management│ │   Processing│ │   Mgmt      │ │   Manager   │  │   │
    │  │  │ - Status    │ │ - Restaurant│ │ - Driver    │ │ - Driver    │ │ - Push      │  │   │
    │  │  │   Tracking  │ │   Status    │ │   Tracking  │ │   Profiles  │ │   Notifications│ │   │
    │  │  │ - Payment   │ │ - Availability│ │ - Real-time│ │ - Auth      │ │ - Email     │  │   │
    │  │  │   Processing│ │   Updates   │ │   Updates   │ │ - Session   │ │   Alerts    │  │   │
    │  │  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘  │   │
    │  └─────────────────────────────────────────────────────────────────────────────────┘   │
    │                                         │                                               │
    │  ┌─────────────────────────────────────▼───────────────────────────────────────────┐   │
    │  │                       Data Access Layer                                         │   │
    │  │                                                                                  │   │
    │  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐  │   │
    │  │  │   Order     │ │ Restaurant  │ │  Location   │ │    User     │ │   Shared    │  │   │
    │  │  │ Repository  │ │ Repository  │ │ Repository  │ │ Repository  │ │ Repository  │  │   │
    │  │  │             │ │             │ │             │ │             │ │             │  │   │
    │  │  │ - CRUD Ops  │ │ - Menu Cache│ │ - Batch     │ │ - Profile   │ │ - Common    │  │   │
    │  │  │ - Order     │ │ - Status    │ │   Inserts   │ │   Data      │ │   Queries   │  │   │
    │  │  │   Queries   │ │   Updates   │ │ - Location  │ │ - Auth      │ │ - Transactions│ │   │
    │  │  │ - Status    │ │ - Query     │ │   Queries   │ │   Tokens    │ │ - Migrations│  │   │
    │  │  │   Updates   │ │   Optimization│ │ - History  │ │ - Session   │ │ - Utilities │  │   │
    │  │  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘  │   │
    │  └─────────────────────────────────────────────────────────────────────────────────┘   │
    └─────────────────────────────────────────────────────────────────────────────────────────┘
                                         │
    ┌─────────────────────────────────────▼───────────────────────────────────────────────────┐
    │                              Infrastructure Layer                                       │
    │                                                                                          │
    │  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐          │
    │  │              │    │              │    │              │    │              │          │
    │  │ PostgreSQL   │    │    Redis     │    │  RabbitMQ    │    │   WebSocket  │          │
    │  │              │    │              │    │              │    │   Manager    │          │
    │  │ - Primary DB │    │ - Menu Cache │    │ - GPS Events │    │              │          │
    │  │ - Orders     │    │ - Session    │    │ - Async Tasks│    │ - Driver     │          │
    │  │ - Restaurants│    │   Storage    │    │ - Email Queue│    │   Locations  │          │
    │  │ - Users      │    │ - Rate       │    │ - Notification│    │ - Order      │          │
    │  │ - Drivers    │    │   Limiting   │    │   Queue      │    │   Updates    │          │
    │  │ - Locations  │    │ - App Cache  │    │              │    │              │          │
    │  │              │    │              │    │              │    │              │          │
    │  └──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘          │
    └─────────────────────────────────────────────────────────────────────────────────────────┘
    
    External Interfaces:
    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
    │   Customer  │    │ Restaurant  │    │   Driver    │    │   Admin     │
    │     App     │    │   Portal    │    │     App     │    │   Portal    │
    └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

---

## Component Responsibilities

### 1. API Gateway Layer
**Purpose**: Request routing, authentication, rate limiting, and response formatting

**Responsibilities:**
- Route incoming requests to appropriate business modules
- Handle authentication and authorization (JWT tokens)
- Implement rate limiting to prevent abuse
- Request/response validation and transformation
- API versioning and documentation serving
- CORS handling for web clients

### 2. Order Module
**Purpose**: Complete order lifecycle management

**Responsibilities:**
- Order creation and validation
- Payment processing integration (mocked)
- Order status tracking and updates
- Order assignment to drivers
- Order history and analytics
- Refund and cancellation handling

**Key Operations:**
- `createOrder()`: Validate and create new orders
- `updateOrderStatus()`: Track order progression
- `assignDriver()`: Match orders with available drivers
- `processPayment()`: Handle payment workflow
- `getOrderHistory()`: Retrieve customer/restaurant order history

### 3. Restaurant Module
**Purpose**: Restaurant and menu management with high-performance browsing

**Responsibilities:**
- Restaurant profile management
- Menu item management (CRUD operations)
- Restaurant availability status updates
- Menu caching and cache invalidation
- Search and filtering capabilities
- Restaurant analytics and reporting

**Performance Optimizations:**
- Aggressive Redis caching for menu data
- Database query optimization with indexes
- Menu data denormalization for fast reads
- Background cache warming processes

### 4. Location Module
**Purpose**: High-throughput GPS data processing and real-time tracking

**Responsibilities:**
- GPS coordinate validation and processing
- Driver location storage and retrieval
- Real-time location broadcasting to customers
- Location history maintenance for analytics
- Geospatial queries for driver matching
- Location-based ETAs and routing

**Performance Strategy:**
- RabbitMQ for GPS event buffering
- Batch processing for database writes
- In-memory location cache for real-time queries
- Periodic cleanup of historical location data

### 5. User Module
**Purpose**: Customer and driver profile management

**Responsibilities:**
- User registration and profile management
- Authentication and session management
- Role-based access control
- Driver verification and onboarding
- Customer preferences and settings
- User analytics and behavior tracking

### 6. Notification Module
**Purpose**: Real-time communication with users

**Responsibilities:**
- WebSocket connection management
- Push notification delivery
- Email notification queuing
- SMS alert processing (if required)
- Notification template management
- Delivery confirmation tracking

---

## Data Flow Descriptions

### 1. Order Processing Flow
```
Customer → API Gateway → Order Module → Payment Module → Restaurant Module → Driver Assignment
    ↓           ↓             ↓              ↓               ↓                    ↓
Database ← Notification ← Status Update ← Payment Confirm ← Inventory Check ← Driver Match
```

**Detailed Steps:**
1. Customer submits order through API Gateway
2. Order Module validates order details and inventory
3. Payment Module processes mock payment
4. Restaurant Module confirms order acceptance
5. Driver matching algorithm assigns available driver
6. Real-time notifications sent to all parties
7. Order status tracking begins

### 2. Menu Browsing Flow
```
Customer → API Gateway → Restaurant Module → Redis Cache → Database (if cache miss)
                              ↓
                         Cached Response (P99 < 200ms)
```

**Cache Strategy:**
- Menu data cached in Redis with 15-minute TTL
- Cache warming during off-peak hours
- Proactive cache invalidation on menu updates
- Cache-aside pattern for cache misses

### 3. Real-time Location Tracking Flow
```
Driver App → API Gateway → Location Module → RabbitMQ → Batch Processor → Database
                              ↓
                         WebSocket Manager → Customer App (real-time updates)
```

**Processing Pipeline:**
1. Driver sends GPS coordinates every 5 seconds
2. Location Module validates and queues events in RabbitMQ
3. Background workers batch process events to database
4. WebSocket Manager broadcasts updates to tracking customers
5. Location history maintained for analytics

---

## Technology Stack Justification

### Primary Technologies (Enterprise-ready, Simple Locally)

#### **Java 17 + Spring Boot 3**
**Rationale:**
- Mature, production-proven platform with strong ecosystem and community support
- Excellent performance characteristics with **Netty/Tomcat** and **HikariCP**
- Rich first-class integrations: Spring Data JPA, Spring AMQP (RabbitMQ), Spring WebSocket, Spring Security, Spring Validation, Spring Actuator
- Strong typing and tooling for enterprise-grade maintainability
- Easy local validation with embedded servers and Dockerized dependencies

**Spring Modules Used:**
- Spring Web (REST APIs)
- Spring Data JPA (PostgreSQL, PostGIS)
- Spring AMQP (RabbitMQ integration)
- Spring WebSocket (STOMP/SockJS for real-time updates)
- Spring Security (JWT-based auth)
- Spring Validation (Bean Validation/Hibernate Validator)
- Spring Boot Actuator (health/metrics)

**Production posture:**
- Enforce security best practices (CORS, headers, CSRF where applicable)
- Centralized config/secrets (Vault/SM); rolling updates via orchestration
- Blue/green or canary deploys; readiness/liveness probes

**Local simplicity:**
- Single JVM process; properties via `.env`/application.properties
- Actuator endpoints exposed locally; no external config system required

#### **PostgreSQL Database**
**Rationale:**
- ACID compliance ensures order data consistency
- Excellent performance for 500 orders/minute load
- Strong geospatial support (PostGIS) for location queries
- Robust backup and replication capabilities
- Cost-effective with proven reliability

**Alternative Considered:** MongoDB - Rejected due to eventual consistency concerns for financial transactions

**Production posture:**
- Managed Postgres (e.g., RDS/Aurora/Cloud SQL) with PITR, automated backups
- High availability (multi-AZ), read replicas for browse traffic, partitioning where needed
- Strict migration discipline with Flyway, connection limits via HikariCP

**Local simplicity:**
- Single dockerized Postgres with PostGIS enabled; seeded via Flyway on startup

#### **Redis Cache**
**Rationale:**
- Sub-millisecond response times for menu browsing
- Built-in data structures perfect for caching patterns
- Pub/Sub capabilities for real-time features
- Session storage with automatic expiration
- Memory-efficient for target dataset size

**Alternative Considered:** Memcached - Rejected due to lack of advanced data structures

**Production posture:**
- HA Redis (Sentinel or managed) with persistence where appropriate; key TTL policies
- Separate logical DBs for cache, sessions, and rate limiting; memory monitoring/alerts

**Local simplicity:**
- Single Redis container; default config; ephemeral data acceptable

#### **RabbitMQ Message Queue**
**Rationale:**
- More than sufficient for 2,000 events/second throughput
- Lower operational complexity compared to Kafka
- Excellent reliability with message persistence
- Dead letter queues for error handling
- Significantly lower resource requirements

**Alternative Considered:** Apache Kafka - Rejected as over-engineered for current scale (designed for 100K+ events/second)

**Production posture:**
- Clustered RabbitMQ with quorum queues, mirrored policies, DLX/DLQ monitoring
- Publisher confirms and consumer acks; retry/backoff via dead-letter exchanges

**Local simplicity:**
- Single RabbitMQ container with management UI; basic durable queue config

#### **Spring WebSocket (STOMP/SockJS)**
**Rationale:**
- Native Spring integration with messaging abstractions (simpMessagingTemplate)
- STOMP topics/queues for order-room updates and driver streams
- SockJS fallback support for older clients
- Production-ready with interceptors, security, and session management

**Production posture:**
- Sticky sessions or external session store; connection limits and keepalive tuning
- Authenticated destinations; rate-limit fan-out; horizontal scale with shared message broker

**Local simplicity:**
- Single-node WebSocket endpoint; in-memory session registry

#### **Testing & Quality Tooling**
**Production posture:**
- JUnit 5, Testcontainers (Postgres/Redis/RabbitMQ), WireMock for external stubs
- Static analysis: SpotBugs, Checkstyle; dependency scanning: OWASP Dependency-Check
- Performance testing: Gatling/JMeter with CI gate on SLAs

**Local simplicity:**
- Run tests with embedded or Testcontainers; simple `./mvnw test` or `./gradlew test`

---

## Performance Targets and Optimization

### Target Metrics
- **Order Processing**: 500 orders/minute (8.33/second) sustained load
- **Menu Browsing**: P99 response time < 200ms
- **Location Processing**: 2,000 GPS events/second with < 50ms latency
- **Database Queries**: P95 < 100ms for all read operations
- **Memory Usage**: < 2GB RAM for local demonstration

### Optimization Strategies

#### **Database Optimization**
```sql
-- Order table indexes for performance
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_restaurant_id ON orders(restaurant_id);

-- Location table optimization
CREATE INDEX idx_locations_driver_timestamp ON locations(driver_id, created_at);
CREATE INDEX idx_locations_geospatial ON locations USING GIST(coordinates);

-- Restaurant menu caching strategy
CREATE INDEX idx_menu_items_restaurant_available ON menu_items(restaurant_id, is_available);
```

#### **Caching Strategy**
- **Menu Data**: 15-minute TTL with proactive invalidation
- **Restaurant Status**: 5-minute TTL with real-time updates
- **User Sessions**: 24-hour TTL with sliding expiration
- **Location Data**: 30-second TTL for customer tracking

#### **Connection Pooling**
```properties
# HikariCP connection pool (application.properties)
spring.datasource.url=jdbc:postgresql://postgres:5432/swifteats
spring.datasource.username=admin
spring.datasource.password=password
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=2000
spring.datasource.hikari.idle-timeout=30000
```

#### **RabbitMQ Configuration**
```java
// Spring AMQP configuration (Java Config)
@Bean
public Queue gpsEventsQueue() {
  return QueueBuilder.durable("gps-events")
      .withArgument("x-max-length", 10000)
      .withArgument("x-message-ttl", 60000)
      .withArgument("x-dead-letter-exchange", "gps-events-dlx")
      .build();
}

@Bean
public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
  SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
  factory.setConnectionFactory(connectionFactory);
  factory.setPrefetchCount(100); // batch-like consumption
  factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
  return factory;
}
```

---

## Resilience and Failure Handling

### Failure Scenarios and Mitigation

#### **Database Connection Failure**
**Detection**: Connection pool monitoring and health checks
**Mitigation**:
- Automatic connection retry with exponential backoff
- Read-only mode using cached data for non-critical operations
- Graceful degradation with user-friendly error messages

```javascript
// Database circuit breaker implementation
const circuitBreaker = new CircuitBreaker(dbQuery, {
  timeout: 3000,
  errorThresholdPercentage: 50,
  resetTimeout: 30000
});
```

#### **Redis Cache Failure**
**Detection**: Cache operation timeouts and error monitoring
**Mitigation**:
- Direct database fallback for cache misses
- Cache reconstruction from database on recovery
- Performance degradation alerts to operations team

#### **RabbitMQ Queue Failure**
**Detection**: Queue depth monitoring and message processing rates
**Mitigation**:
- In-memory buffer as temporary storage
- Dead letter queues for failed message processing
- Manual queue drain procedures for recovery

#### **High Load Scenarios**
**Detection**: Response time monitoring and throughput metrics
**Mitigation**:
- Request rate limiting with Redis counters
- Horizontal pod autoscaling in production
- Load shedding for non-critical operations

### Health Check Implementation
```properties
# Spring Boot Actuator (application.properties)
management.endpoints.web.exposure.include=health,metrics,info,env
management.endpoint.health.show-details=always
```

```java
// Custom Health Indicators
@Component
public class RabbitHealthIndicator implements HealthIndicator {
  private final RabbitTemplate rabbitTemplate;
  public RabbitHealthIndicator(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }
  @Override public Health health() {
    try { rabbitTemplate.execute(channel -> { channel.isOpen(); return null; }); return Health.up().build(); }
    catch (Exception e) { return Health.down(e).build(); }
  }
}
```

---

## Local Validation and Testing

---

## Technology Recommendations by Component

### Summary Choices (Java-first)
- **API & Business Logic**: Java 17 + Spring Boot 3 (Spring Web, Spring Validation, Spring Security)
- **Persistence**: PostgreSQL 14+ with PostGIS; Spring Data JPA/Hibernate; Flyway migrations
- **Caching**: Redis 6+ (in-memory cache, sessions, rate-limiting)
- **Messaging**: RabbitMQ 3.9+ (GPS buffering, async tasks, notifications)
- **Real-time**: Spring WebSocket (STOMP/SockJS)
- **Build/Packaging**: Gradle or Maven; Jib for container images
- **Observability**: Spring Boot Actuator, Micrometer + Prometheus (optional), structured logs (Logback)

### Component-by-Component Analysis

1) API Gateway Layer (within monolith)
- Technology: Spring Boot (Spring Web), Spring Security (JWT), Springdoc OpenAPI
- Advantages:
  - Tight integration with domain modules; minimal overhead
  - Mature authN/authZ stack (filters, method security)
  - Auto-generated API docs with OpenAPI
  - Simple rate-limiting via Redis counters
  
- Disadvantages:
  - Less flexibility than an external API gateway
  - App-level rate limiting may contend for app resources
 
 - Production posture:
   - JWT with key rotation (JWKS), CORS/CSRF policies, WAF/CDN fronting
   - Centralized auth via OIDC provider (Keycloak/Auth0) if needed
 - Local simplicity:
   - In-app JWT verification with static test keys; OpenAPI UI at /swagger-ui

2) Order Module
- Technology: Spring Boot, Spring Data JPA, PostgreSQL, Flyway
- Advantages:
  - Strong ACID guarantees for order lifecycle
  - Rich JPA/Hibernate tooling and ecosystem
  - Simple schema migration via Flyway
  
- Disadvantages:
  - Requires careful query tuning to avoid N+1 issues
  - Schema evolution needs strict discipline
 
 - Production posture:
   - Read/write separation where required; idempotent order creation endpoints
 - Local simplicity:
   - H2 disabled; always use local Postgres via Docker for parity

3) Restaurant/Menu Module
- Technology: Spring Boot, PostgreSQL + Redis cache
- Advantages:
  - Sub-200ms P99 with Redis cache-aside
  - Indexing and partial denormalization possible in Postgres
  
- Disadvantages:
  - Cache invalidation complexity on frequent menu updates
 
 - Production posture:
   - Cache keys namespaced; proactive invalidation on updates; metrics on hit ratio
 - Local simplicity:
   - Single Redis instance; default TTLs from properties

4) Location Module
- Technology: Spring Boot, RabbitMQ (Spring AMQP), PostgreSQL (PostGIS), Redis for hot location cache
- Advantages:
  - RabbitMQ is cost-efficient, reliable for 2k events/sec
  - PostGIS enables fast geo-queries (nearest-driver, bounding boxes)
  - Redis supports hot reads for live map updates
  
- Disadvantages:
  - Backpressure needs tuning (prefetch, DLQ policies)
  - Batch writes must be carefully sized to balance latency and throughput
 
 - Production posture:
   - Quorum queues; publisher confirms; consumer concurrency controls
   - Partition drivers by shard key if needed; Postgres table partitioning by time
 - Local simplicity:
   - Single queue; simple consumer; batch size configurable via properties

5) Notification Module
- Technology: Spring WebSocket (STOMP), optional Redis pub/sub for fan-out
- Advantages:
  - Native integration; rooms via STOMP destinations
  - Low-latency updates to customers and drivers
  
- Disadvantages:
  - WebSocket scale requires connection lifecycle management
 
 - Production posture:
   - External session store; STOMP broker relay if needed; connection quotas
 - Local simplicity:
   - Simple in-memory STOMP broker; single instance

6) User/Auth Module
- Technology: Spring Security (JWT), Spring Data JPA
- Advantages:
  - Battle-tested auth; role-based access control
  - Stateless JWTs simplify scaling
  
- Disadvantages:
  - JWT revocation lists require Redis or short TTL
 
 - Production posture:
   - Password hashing (bcrypt/argon2), MFA optional, audit logs
 - Local simplicity:
   - Pre-seeded users; dev JWT secret; simple role matrix

7) Caching & Rate Limiting
- Technology: Redis (Spring Data Redis, Lettuce client)
- Advantages:
  - Microsecond ops; counters, hashes for structured cache
  - Easy to implement leaky-bucket/token-bucket rate limiting
  
- Disadvantages:
  - Requires HA if used for sessions; memory sizing needed
 
 - Production posture:
   - Separate instance/pool for session vs cache; eviction policies per keyspace
 - Local simplicity:
   - Single instance; default eviction; docker-compose volume optional

8) Database
- Technology: PostgreSQL + PostGIS
- Advantages:
  - ACID, strong consistency; rich indexing; geospatial
  - Cost-effective; easy local and cloud deployment
  
- Disadvantages:
  - Write scaling beyond a point needs sharding/partitioning strategy
 
 - Production posture:
   - Backups, PITR, HA; auto-vacuum tuned; partitioning for time-series tables
 - Local simplicity:
   - One container; Flyway auto-migrate on app start

9) Messaging
- Technology: RabbitMQ
- Advantages:
  - Simple ops; supports DLQ, retries, priorities
  - Adequate throughput and latency for our needs
  
- Disadvantages:
  - Not designed for massive streaming analytics (Kafka territory)
 
 - Production posture:
   - Quorum queues, mirrored policies, DLQ alarms; TLS connections
 - Local simplicity:
   - Single-node; management UI enabled for visibility

10) Observability
- Technology: Spring Actuator, Micrometer; Logback JSON logs
- Advantages:
  - Minimal setup; health, metrics, traces ready
  
- Disadvantages:
  - Advanced tracing requires extra tooling (e.g., OpenTelemetry)
 
 - Production posture:
   - Prometheus/Grafana, OpenTelemetry exporters, log aggregation (ELK/Cloud)
 - Local simplicity:
   - Actuator exposure; simple logs to console/file; optional dockerized Prometheus

---

## Alternatives and Trade-offs

- API/Framework: Spring Boot vs Node.js/Express
  - Spring: +strong typing, mature ecosystem; -steeper learning for juniors
  - Node: +fast prototyping; -less suitable for CPU-heavy tasks

- Messaging: RabbitMQ vs Kafka
  - RabbitMQ: +simple, cost-efficient, fits 2k eps; -less suited for big-data streams
  - Kafka: +massive throughput, replay; -overkill/complex for current needs

- Cache: Redis vs Memcached
  - Redis: +richer data structures, pub/sub; -slightly heavier footprint
  - Memcached: +simple; -limited feature set

- DB: PostgreSQL vs MongoDB
  - Postgres: +ACID, joins, geospatial; -schema changes need migrations
  - Mongo: +flexible schema; -eventual consistency risks for orders/payments

---

## Technology Evaluation Matrix

| Category | Option | Scalability | Resilience | Performance | Dev Productivity | Operability | Cost | Production Readiness | Local Simplicity | Notes |
|---------|--------|------------:|-----------:|------------:|-----------------:|------------:|-----:|--------------------:|-----------------:|-------|
| API/Framework | Spring Boot | 4 | 4 | 4 | 4 | 4 | 4 | 5 | 4 | Enterprise-grade, great integrations |
| API/Framework | Node.js/Express | 4 | 3 | 4 | 5 | 4 | 4 | 4 | 5 | Fast to prototype; JS tooling |
| Database | PostgreSQL + PostGIS | 4 | 4 | 4 | 4 | 4 | 4 | 5 | 4 | ACID + geospatial; proven |
| Database | MongoDB | 3 | 3 | 3 | 4 | 4 | 4 | 3 | 4 | Flexible but eventual consistency |
| Cache | Redis | 4 | 4 | 5 | 4 | 4 | 4 | 5 | 5 | Rich features; sub-ms |
| Cache | Memcached | 3 | 3 | 4 | 4 | 4 | 4 | 3 | 5 | Simple cache only |
| Messaging | RabbitMQ | 4 | 4 | 4 | 4 | 4 | 4 | 5 | 5 | DLQ, retries; fits 2k eps |
| Messaging | Kafka | 5 | 4 | 5 | 3 | 3 | 3 | 5 | 2 | Overkill now; future option |
| Realtime | Spring WebSocket | 4 | 4 | 4 | 4 | 4 | 4 | 4 | 5 | Native Spring, STOMP |
| Observability | Spring Actuator+Micrometer | 4 | 4 | 4 | 5 | 4 | 4 | 5 | 5 | Minimal setup, extensible |

Scoring legend: 1=Poor, 3=Good, 5=Excellent.

### Development Environment Setup

#### **System Requirements**
- **RAM**: 2GB minimum, 4GB recommended
- **CPU**: 2 cores minimum
- **Storage**: 10GB for containers and data
- **OS**: macOS, Linux, or Windows with Docker support

#### **Docker Compose Configuration**
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "3000:3000"
    environment:
      - NODE_ENV=development
      - DB_HOST=postgres
      - REDIS_HOST=redis
      - RABBITMQ_HOST=rabbitmq
    depends_on:
      - postgres
      - redis
      - rabbitmq
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3000/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  postgres:
    image: postgis/postgis:13-3.1
    environment:
      POSTGRES_DB: swifteats
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:6.2-alpine
    ports:
      - "6379:6379"
    command: redis-server --maxmemory 256mb --maxmemory-policy allkeys-lru

  rabbitmq:
    image: rabbitmq:3.9-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: password

volumes:
  postgres_data:
```

### Testing Strategy

#### **Load Testing Configuration**
```java
// Java-based GPS event simulator sketch (pseudo-code)
ExecutorService pool = Executors.newFixedThreadPool(8);
int drivers = 50; int eps = 10; int durationSec = 300;
for (int d = 0; d < drivers; d++) {
  int driverId = d;
  pool.submit(() -> {
    long end = System.currentTimeMillis() + durationSec * 1000L;
    while (System.currentTimeMillis() < end) {
      for (int i = 0; i < eps; i++) {
        GpsEvent e = GpsEvent.randomAround(18.5204, 73.8567, driverId);
        restTemplate.postForEntity(baseUrl + "/location", e, Void.class);
      }
      Thread.sleep(1000);
    }
  });
}
```

#### **Performance Validation**
- **Menu Browsing**: 1000 concurrent requests with P99 timing
- **Order Processing**: 500 orders in 60 seconds sustained test
- **Location Processing**: 50 drivers × 10 events/second for 5 minutes
- **Memory Profiling**: Continuous monitoring during load tests

#### **Integration Test Suite**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderFlowIT {
  @Autowired TestRestTemplate rest;

  @Test void endToEndOrderUnderLoad() {
    List<OrderRequest> batch = IntStream.range(0, 100)
      .mapToObj(i -> Fixtures.newOrder())
      .toList();
    long start = System.currentTimeMillis();
    List<ResponseEntity<OrderResponse>> responses = batch.stream()
      .map(req -> rest.postForEntity("/api/orders", req, OrderResponse.class))
      .toList();
    long avgMs = (System.currentTimeMillis() - start) / Math.max(1, responses.size());
    assertTrue(responses.stream().allMatch(r -> r.getStatusCode().is2xxSuccessful()));
    assertTrue(avgMs < 500);
  }
}
```

### Monitoring and Observability

#### **Local Monitoring Dashboard**
- **Application Metrics**: Request rates, response times, error rates
- **System Metrics**: CPU, memory, disk usage
- **Database Metrics**: Query performance, connection pool status
- **Queue Metrics**: Message rates, queue depths, processing times

#### **Logging Configuration**
```javascript
const winston = require('winston');

const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.errors({ stack: true }),
    winston.format.json()
  ),
  transports: [
    new winston.transports.Console(),
    new winston.transports.File({ filename: 'logs/app.log' })
  ]
});
```

This architecture provides a solid foundation for SwiftEats' immediate needs while maintaining clear evolution paths for future growth, ensuring cost-effective operation without compromising on reliability or performance requirements.

---

## Detailed System Design

### 1) Domain Model

Core entities with key attributes and constraints. All tables have `created_at`, `updated_at`, and optimistic lock `version`.

- `users`
  - id (UUID, PK), email (uniq), phone (uniq), password_hash, role (CUSTOMER|DRIVER|RESTAURANT|ADMIN), status
  - Indexes: email, phone, role

- `restaurants`
  - id (UUID, PK), name, status (OPEN|CLOSED|BUSY), address, city, geo (PostGIS POINT), hours_json
  - Indexes: status, city, geo (GIST)

- `menu_items`
  - id (UUID, PK), restaurant_id (FK), name, description, price, is_available, prep_time_seconds
  - Indexes: restaurant_id, (restaurant_id, is_available)

- `orders`
  - id (UUID, PK), customer_id (FK users), restaurant_id (FK), driver_id (nullable FK users), status (PLACED|CONFIRMED|PREPARING|READY|PICKED_UP|DELIVERED|CANCELLED), total_amount, payment_status (PENDING|SUCCESS|FAILED), eta_seconds
  - Indexes: customer_id, restaurant_id, driver_id, status, created_at

- `order_items`
  - id (UUID, PK), order_id (FK orders), menu_item_id (FK), quantity, unit_price
  - Indexes: order_id

- `driver_locations`
  - id (bigserial PK), driver_id (FK users), coordinates (PostGIS POINT), accuracy_m, recorded_at (timestamptz)
  - Indexes: (driver_id, recorded_at), coordinates GIST

- `payments_mock`
  - id (UUID PK), order_id (FK), status (SUCCESS|FAILED), gateway_ref, processed_at
  - Indexes: order_id, status

Relationships and rules:
- `orders.customer_id` required; `orders.driver_id` set on assignment
- `menu_items.restaurant_id` required; cascade delete disabled (protect historical orders)
- Soft delete avoided; use status flags for availability

### 2) API Surface (High Level)

Notation: `METHOD /path` – purpose (Auth: role)

- Orders
  - `POST /api/orders` – create order (Auth: CUSTOMER; idempotent key header `Idempotency-Key`)
  - `GET /api/orders/{orderId}` – fetch order status (Auth: CUSTOMER/RESTAURANT/DRIVER by ownership)
  - `PATCH /api/orders/{orderId}/status` – update status (Auth: RESTAURANT/DRIVER)

- Payments (Mock)
  - `POST /api/payments/{orderId}` – simulate payment (Auth: CUSTOMER)

- Restaurants & Menu
  - `GET /api/restaurants` – list/search restaurants (Auth: ANY)
  - `GET /api/restaurants/{id}/menu` – fetch menu with status (Auth: ANY)
  - `POST /api/restaurants/{id}/menu` – manage menu items (Auth: RESTAURANT)
  - `PATCH /api/restaurants/{id}/status` – update open/busy/closed (Auth: RESTAURANT)

- Drivers & Location
  - `POST /api/drivers/{driverId}/location` – post GPS point (Auth: DRIVER)
  - `GET /api/orders/{orderId}/track` – SSE/WebSocket subscription endpoint (Auth: CUSTOMER)

- Users/Auth
  - `POST /api/auth/login` – login (JWT)
  - `POST /api/auth/register` – register

Validation: Bean Validation annotations; consistent error format `{ code, message, details[] }`.

### 3) Messaging Topology (RabbitMQ)

- Exchanges
  - `gps.events` (direct): routing key `driver.{driverId}`; bound to `gps.events.q`
  - `notify.events` (topic): order events `order.*` to notify queue

- Queues
  - `gps.events.q` (durable, quorum)
    - DLX: `gps.events.dlx`, DLQ: `gps.events.dlq`
    - Args: `x-max-length=10000`, `x-message-ttl=60000`
  - `notify.events.q` (durable)

- Consumers
  - `GpsIngestor` (concurrency N, prefetch 100) – validate and batch insert to Postgres
  - `Notifier` – fan-out to WebSocket topics (e.g., `/topic/order.{orderId}`)

Retry policy: immediate requeue 2x, then to DLQ; dead-letter processor with alerting.

### 4) Caching Strategy (Redis)

- Keys
  - `menu:{restaurantId}` – JSON menu (TTL 900s)
  - `rest:status:{restaurantId}` – OPEN/CLOSED/BUSY (TTL 300s)
  - `track:driver:{driverId}` – latest GPS (TTL 60s)
  - `session:{userId}` – session data (TTL 24h)
  - `rate:{userId}:{endpoint}` – sliding window counters

- Patterns
  - Cache-aside for menu and restaurant status
  - Proactive invalidation on management updates
  - Read-through for tracking with TTL refresh on update

### 5) Security & Governance

- AuthN/AuthZ: Spring Security with JWT (RS256), roles: CUSTOMER, DRIVER, RESTAURANT, ADMIN
- Idempotency: `Idempotency-Key` header for `POST /orders` stored in Redis for 24h
- Rate limiting: token bucket per user/IP in Redis (sane defaults, e.g., 100 req/min)
- Input validation: Bean Validation + centralized exception handler
- Secrets: environment variables locally; managed secrets in production
- PII: encryption at rest (DB-level), TLS in transit, minimal logging of PII

### 6) Transaction & Consistency Model

- Orders: single-transaction create (order + items), payment mock update transactional
- Status updates: transactional with append-only audit table `order_events`
- Location writes: eventually consistent via queue; reads prefer Redis hot location then DB

### 7) Performance Budgets

- Menu GET P99 < 200ms: 90ms DB (cache miss) + 20ms network + 30ms app + headroom; cache hit < 50ms
- Orders: create < 500ms at 500/min sustained; DB writes amortized, indexes kept small
- GPS ingest: 2,000 eps target; per-node consumer ~500 eps with prefetch 100; scale consumers to 4

### 8) Capacity Planning (Initial)

- App: 2 instances (2 vCPU, 2–4GB RAM) behind LB
- Postgres: 2 vCPU, 4–8GB RAM; 1 read replica optional; storage with provisioned IOPS
- Redis: 1 vCPU, 1–2GB RAM; persistence optional
- RabbitMQ: 1–2 vCPU, 2GB RAM; quorum queues if clustered later

### 9) Observability

- Metrics (Micrometer):
  - http.server.requests (P50/P95/P99), order.create.timer, gps.ingest.rate, cache.hit/miss, db.pool.{inUse, idle}
- Logs: JSON with correlationId, userId, orderId; error stack traces
- Health: Actuator health groups for db, redis, rabbit; readiness/liveness endpoints
- Alerts (prod): SLA breach on menu P99>200ms, gps lag, queue depth, DB errors, error rate > 2%

### 10) Testing & CI/CD Gates

- Unit: JUnit5 + Mockito; coverage target ≥ 80%
- Integration: Testcontainers (Postgres/Redis/RabbitMQ); happy-path and failure paths
- Contract/API: Spring MVC tests + OpenAPI validation
- Performance: Gatling/JMeter scenarios for menu, orders, GPS ingest
- Security: Static analysis (SpotBugs), dependency scan (OWASP), secrets scan
- CI gates: lint/static analysis pass; unit+integration tests green; performance smoke within SLOs

### 11) Data Migration & Lifecycle

- Migrations via Flyway; semantic versioning of DB changes
- Archival policies for `driver_locations` (time-partitioned tables; retention 90 days)
- Backups: daily full + WAL for PITR; restore testing quarterly

### 12) Failure Scenarios (Concrete)

- Postgres degraded IO: switch to cached reads for menus; reduce write batch size; alert DBA
- Redis unavailable: degrade to DB reads; disable rate limiting temporarily; notify ops
- RabbitMQ backlog: increase consumers; apply backpressure (429) on GPS endpoint; purge DLQ after inspection
- App instance crash: LB routes to healthy instance; in-flight messages re-delivered

### 13) Deployment Profiles

- Local: docker-compose single-node deps; `spring.profiles.active=local`; seeded data
- Staging: production-like with smaller quotas; synthetic load jobs
- Production: managed services, HA where required; blue/green rollout with health checks