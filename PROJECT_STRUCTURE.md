# SwiftEats Project Structure

This document explains the structure of the SwiftEats backend project and the purpose of each folder and key module.

## Root Directory Structure

```
swifteats-backend/
├── src/                        # Source code
│   ├── main/                   # Main application code
│   └── test/                   # Test code
├── docker/                     # Docker configuration files
├── monitoring/                 # Monitoring configuration
├── nginx/                      # Load balancer configuration
├── docs/                       # Additional documentation
├── target/                     # Build output (generated)
├── .mvn/                       # Maven wrapper
├── pom.xml                     # Maven dependencies and build configuration
├── Dockerfile                  # Docker image definition
├── docker-compose.yml          # Multi-service Docker setup
├── README.md                   # Project overview and setup instructions
├── ARCHITECTURE.md             # Detailed system architecture
├── PROJECT_STRUCTURE.md        # This file
└── API-SPECIFICATION.yml       # OpenAPI specification
```

## Source Code Structure

### Main Application (`src/main/java/com/swifteats/`)

```
src/main/java/com/swifteats/
├── SwiftEatsApplication.java   # Main Spring Boot application class
├── config/                     # Configuration classes
│   ├── AsyncConfig.java        # Async processing configuration
│   ├── CacheConfig.java        # Redis cache configuration
│   └── WebSocketConfig.java    # WebSocket configuration for real-time features
├── controller/                 # REST API controllers
│   ├── RestaurantController.java    # Restaurant and menu endpoints
│   ├── OrderController.java         # Order management endpoints
│   └── DriverController.java        # Driver and location endpoints
├── service/                    # Business logic layer
│   ├── RestaurantService.java       # Restaurant and menu business logic
│   ├── OrderService.java            # Order processing logic
│   ├── DriverService.java           # Driver management and location tracking
│   ├── DriverAssignmentService.java # Intelligent driver assignment
│   └── PaymentService.java          # Payment processing (mocked)
├── repository/                 # Data access layer
│   ├── RestaurantRepository.java    # Restaurant data access
│   ├── MenuItemRepository.java      # Menu item queries
│   ├── OrderRepository.java         # Order data access
│   ├── CustomerRepository.java      # Customer data access
│   ├── DriverRepository.java        # Driver data access
│   └── DriverLocationRepository.java # Location data access
├── model/                      # Domain entities
│   ├── Restaurant.java              # Restaurant entity
│   ├── MenuItem.java                # Menu item entity
│   ├── Order.java                   # Order entity
│   ├── OrderItem.java               # Order item entity
│   ├── Customer.java                # Customer entity
│   ├── Driver.java                  # Driver entity
│   └── DriverLocation.java          # Location tracking entity
├── dto/                        # Data Transfer Objects
│   ├── RestaurantResponseDto.java   # Restaurant response DTO
│   ├── MenuItemResponseDto.java     # Menu item response DTO
│   ├── OrderResponseDto.java        # Order response DTO
│   ├── CreateOrderRequestDto.java   # Order creation request DTO
│   └── DriverLocationUpdateDto.java # Location update DTO
├── exception/                  # Custom exceptions
│   ├── OrderNotFoundException.java  # Order-specific exceptions
│   ├── RestaurantNotFoundException.java
│   └── PaymentProcessingException.java
├── simulator/                  # Data simulation for testing
│   ├── DriverLocationSimulator.java # Driver location simulation
│   └── DataInitializer.java         # Sample data creation
└── websocket/                  # WebSocket handlers
    └── LocationWebSocketHandler.java # Real-time location updates
```

### Resources (`src/main/resources/`)

```
src/main/resources/
├── application.yml             # Main application configuration
├── application-dev.yml         # Development environment config
├── application-prod.yml        # Production environment config
├── static/                     # Static web content (if any)
├── templates/                  # Template files (if any)
└── db/
    └── migration/              # Database migration scripts (Flyway)
        ├── V1__Initial_schema.sql
        ├── V2__Add_indexes.sql
        └── V3__Add_location_tables.sql
```

### Test Structure (`src/test/java/com/swifteats/`)

```
src/test/java/com/swifteats/
├── SwiftEatsApplicationTests.java  # Application integration tests
├── controller/                     # Controller layer tests
│   ├── RestaurantControllerTest.java
│   ├── OrderControllerTest.java
│   └── DriverControllerTest.java
├── service/                        # Service layer tests
│   ├── RestaurantServiceTest.java
│   ├── OrderServiceTest.java
│   ├── DriverServiceTest.java
│   └── PaymentServiceTest.java
├── repository/                     # Repository layer tests
│   ├── RestaurantRepositoryTest.java
│   ├── OrderRepositoryTest.java
│   └── DriverLocationRepositoryTest.java
├── integration/                    # Integration tests
│   ├── OrderFlowIntegrationTest.java
│   ├── DriverTrackingIntegrationTest.java
│   └── MenuBrowsingIntegrationTest.java
└── util/                          # Test utilities
    ├── TestDataBuilder.java
    └── MockitoExtension.java
```

## Key Modules Explanation

### 1. Configuration Module (`config/`)

**Purpose**: Centralized configuration for different aspects of the application.

#### `AsyncConfig.java`
- Configures thread pools for async operations
- Separate pools for different workloads (location updates, payments)
- Optimized for high-throughput operations

#### `CacheConfig.java`
- Redis cache configuration with different TTL strategies
- Cache managers for different data types
- Performance optimization for menu browsing

#### `WebSocketConfig.java`
- WebSocket configuration for real-time features
- STOMP messaging protocol setup
- Real-time driver location broadcasting

### 2. Controller Module (`controller/`)

**Purpose**: REST API endpoints and request handling.

#### `RestaurantController.java`
- Restaurant browsing and search endpoints
- Menu retrieval with caching
- Location-based restaurant discovery
- **Performance Focus**: Sub-200ms response times

#### `OrderController.java`
- Order creation and management
- Order status tracking
- Customer and restaurant order views
- **Scalability Focus**: 500 orders/minute capacity

#### `DriverController.java`
- Driver location update endpoints
- Real-time location tracking
- Driver assignment management
- **Throughput Focus**: 2000 location updates/second

### 3. Service Module (`service/`)

**Purpose**: Business logic and orchestration.

#### `RestaurantService.java`
- Restaurant and menu business logic
- Caching strategy implementation
- Search and filtering logic
- **Cache Integration**: Redis-based performance optimization

#### `OrderService.java`
- Order processing workflow
- Payment integration
- Driver assignment coordination
- **Async Processing**: Non-blocking payment processing

#### `DriverService.java`
- Location update processing
- Driver status management
- Real-time location broadcasting
- **Event Streaming**: Kafka-based location events

#### `DriverAssignmentService.java`
- Intelligent driver-order matching
- Proximity-based assignment
- Performance-based ranking
- **Algorithm**: Multi-factor scoring system

#### `PaymentService.java`
- Mock payment processing
- Payment status management
- Async payment handling
- **Integration Ready**: External payment gateway interface

### 4. Repository Module (`repository/`)

**Purpose**: Data access layer with optimized queries.

#### Key Features:
- **Spatial Queries**: Location-based searches using PostGIS
- **Optimized Indexing**: Performance-tuned database indexes
- **Batch Operations**: Bulk data operations for efficiency
- **Custom Queries**: Hand-tuned queries for performance

### 5. Model Module (`model/`)

**Purpose**: Domain entities representing business objects.

#### Design Principles:
- **JPA Annotations**: Hibernate-based persistence
- **Audit Fields**: Created/updated timestamps
- **Validation**: Bean validation annotations
- **Relationships**: Proper entity relationships

### 6. DTO Module (`dto/`)

**Purpose**: Data transfer objects for API communication.

#### Design Benefits:
- **API Stability**: Decoupled from internal models
- **Performance**: Optimized serialization
- **Validation**: Request validation
- **Documentation**: API documentation support

### 7. Simulator Module (`simulator/`)

**Purpose**: Data generation and testing simulation.

#### `DriverLocationSimulator.java`
- Generates realistic driver movements
- Configurable driver count and update frequency
- Multiple movement patterns (random, circular, linear)
- **Testing**: Validates system under load

#### `DataInitializer.java`
- Creates sample restaurants and menus
- Generates test customers
- Initializes reference data
- **Development**: Quick setup for testing

## Build and Deployment Structure

### Maven Configuration (`pom.xml`)

**Key Dependencies:**
- Spring Boot 3.2.8 with Java 21
- PostgreSQL and H2 database drivers
- Redis and Kafka integration
- WebSocket support
- Monitoring and metrics (Actuator, Prometheus)
- Testing framework (JUnit 5, Testcontainers)

**Build Profiles:**
- `dev`: Development configuration
- `test`: Testing configuration
- `prod`: Production configuration

### Docker Configuration

#### `Dockerfile`
- Multi-stage build for optimized images
- Non-root user for security
- Health checks for monitoring
- JVM optimization for performance

#### `docker-compose.yml`
- Complete development environment
- Service orchestration
- Network configuration
- Volume management
- Health checks and dependencies

### Monitoring Setup (`monitoring/`)

#### `prometheus.yml`
- Metrics collection configuration
- Service discovery setup
- Scrape interval optimization
- Target configuration

### Load Balancer (`nginx/`)

#### `nginx.conf`
- Reverse proxy configuration
- Rate limiting for API protection
- WebSocket proxy support
- SSL termination ready

## Development Workflow

### 1. Local Development
```bash
# Start dependencies
docker-compose up -d postgres redis kafka

# Run application
./mvnw spring-boot:run

# Run tests
./mvnw test
```

### 2. Testing Strategy
- **Unit Tests**: Individual component testing
- **Integration Tests**: Component interaction testing
- **Performance Tests**: Load and stress testing
- **Contract Tests**: API contract validation

### 3. Code Quality
- **Checkstyle**: Code style enforcement
- **SpotBugs**: Static analysis
- **JaCoCo**: Code coverage reporting
- **SonarQube**: Code quality metrics

### 4. Deployment Pipeline
```bash
# Build application
./mvnw clean package

# Build Docker image
docker build -t swifteats-backend .

# Deploy with compose
docker-compose up -d --build
```

## Performance Considerations

### 1. Database Optimization
- **Indexes**: Spatial and composite indexes
- **Connection Pooling**: HikariCP configuration
- **Query Optimization**: Efficient JPA queries
- **Batch Processing**: Bulk operations

### 2. Caching Strategy
- **Menu Caching**: 5-minute TTL for menu data
- **Location Caching**: 30-second TTL for driver locations
- **Restaurant Caching**: 10-minute TTL for restaurant data
- **Cache Warming**: Proactive cache population

### 3. Async Processing
- **Location Updates**: Non-blocking processing
- **Payment Processing**: Async payment handling
- **Event Publishing**: Kafka event streaming
- **Scheduled Tasks**: Background job processing

### 4. Resource Management
- **Thread Pools**: Dedicated pools for different workloads
- **Memory Management**: Optimized JVM settings
- **Connection Limits**: Database connection management
- **Rate Limiting**: API protection

This structure provides a clear separation of concerns while maintaining high performance and scalability for the SwiftEats food delivery platform.
