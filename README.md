# SwiftEats - Real-Time Food Delivery Platform Backend

SwiftEats is a scalable, resilient, and high-performance backend system for a modern food delivery service. Built with Spring Boot and Java 21, it handles peak loads of 500 orders per minute while maintaining sub-200ms response times for menu browsing and real-time GPS tracking for up to 10,000 concurrent drivers.

## 🚀 Key Features

- **High-Performance Order Processing**: Handles 500 orders/minute with async payment processing
- **Sub-200ms Menu Browsing**: Redis-cached restaurant and menu data for optimal user experience
- **Real-Time Driver Tracking**: GPS location updates from 10,000+ drivers at 2,000 events/second
- **Intelligent Driver Assignment**: Automated order-to-driver matching based on location and ratings
- **Live Order Tracking**: WebSocket-based real-time updates for customers and restaurants
- **Comprehensive Monitoring**: Integrated Prometheus metrics and Grafana dashboards
- **Production-Ready**: Docker containerization with load balancing and health checks

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Load Balancer │    │   Spring Boot   │    │   PostgreSQL    │
│     (Nginx)     │───▶│   Application   │───▶│    Database     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │                         │
                              ▼                         ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │     Redis       │    │     Kafka       │
                       │     Cache       │    │  Event Stream   │
                       └─────────────────┘    └─────────────────┘
                              │                         │
                              ▼                         ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │   WebSocket     │    │   Monitoring    │
                       │   Real-time     │    │ (Prometheus/    │
                       │   Updates       │    │   Grafana)      │
                       └─────────────────┘    └─────────────────┘
```

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.8
- **Language**: Java 21 (Latest LTS)
- **Database**: PostgreSQL (All Environments)
- **Cache**: Redis
- **Message Queue**: Apache Kafka
- **Real-time**: WebSockets (STOMP)
- **Monitoring**: Prometheus + Grafana
- **Containerization**: Docker & Docker Compose
- **Load Balancer**: Nginx
- **API Documentation**: OpenAPI 3.0 (Swagger)

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose
- Java 21 (for local development)
- Maven 3.8+ (for local development)

### Running the Complete System

1. **Clone and Navigate**
   ```bash
   cd swifteats-backend
   ```

2. **Start All Services**
   ```bash
   docker-compose up -d
   ```

3. **Wait for Services to be Ready**
   ```bash
   # Check health status
   docker-compose ps
   
   # Watch logs
   docker-compose logs -f swifteats-app
   ```

4. **Access the Application**
   - **API Base URL**: http://localhost:8080/api
   - **Swagger UI**: http://localhost:8080/api/swagger-ui.html
   - **Grafana Dashboard**: http://localhost:3000 (admin/admin)
   - **Prometheus**: http://localhost:9090

### Sample Data

The application automatically creates sample data on startup:
- 6 restaurants with diverse cuisines
- 50 menu items across different categories
- 10 sample customers
- 50 simulated drivers (generating real-time location updates)

## 📊 Performance Metrics

### Target Performance
- **Order Processing**: 500 orders/minute
- **Menu Response Time**: P99 < 200ms
- **Driver Location Updates**: 2,000 events/second
- **Concurrent Drivers**: 10,000 drivers
- **Local Testing**: 50 drivers @ 10 events/second

### Monitoring

Access real-time metrics via Grafana dashboards:
- Order processing rates and latency
- Cache hit rates and performance
- Driver location update frequency
- Database performance metrics
- JVM and application health

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Spring profile | `dev` |
| `SWIFTEATS_SIMULATOR_ENABLED` | Enable driver simulator | `true` |
| `SWIFTEATS_SIMULATOR_DRIVER_COUNT` | Number of simulated drivers | `50` |
| `SWIFTEATS_SIMULATOR_EVENTS_PER_SECOND` | Location update frequency | `10` |

### Environment Profiles

The application supports multiple environment profiles:

- **Default/Local**: Uses PostgreSQL on localhost:5433
- **dev**: Development profile with verbose logging
- **test**: Testing profile with separate database and disabled simulator
- **prod**: Production profile with optimized settings and environment variables

To run with a specific profile:
```bash
# Development profile
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Production profile
export SPRING_PROFILES_ACTIVE=prod
./mvnw spring-boot:run
```

### Key Configuration Files

- `application.yml` - Main application configuration with environment profiles
- `docker-compose.yml` - Service orchestration
- `monitoring/prometheus.yml` - Metrics collection
- `nginx/nginx.conf` - Load balancer configuration

## 🧪 Testing the System

### 1. Order Creation Flow
```bash
# Create a new order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "restaurantId": 1,
    "deliveryAddress": "123 Test Street, Mumbai",
    "orderItems": [
      {
        "menuItemId": 1,
        "quantity": 2
      }
    ]
  }'
```

### 2. Real-time Driver Tracking
```bash
# Get nearby drivers
curl "http://localhost:8080/api/drivers/nearby?latitude=19.0760&longitude=72.8777&radius=5"

# Get driver location updates
curl "http://localhost:8080/api/drivers/1/location"
```

### 3. High-Performance Menu Browsing
```bash
# Get restaurant menu (cached for performance)
curl "http://localhost:8080/api/restaurants/1/menu"

# Search restaurants
curl "http://localhost:8080/api/restaurants/search?query=pizza"
```

### 4. WebSocket Real-time Updates
Connect to `ws://localhost:8080/ws` to receive real-time driver location updates.

## 📈 Load Testing

### Order Processing Test
```bash
# Test order creation load (adjust rate as needed)
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d "{\"customerId\":$((RANDOM%10+1)),\"restaurantId\":$((RANDOM%6+1)),\"deliveryAddress\":\"Test Address\",\"orderItems\":[{\"menuItemId\":$((RANDOM%20+1)),\"quantity\":$((RANDOM%3+1))}]}" &
done
```

### Driver Location Update Test
The built-in simulator automatically generates location updates. Monitor performance via:
```bash
# Check location update statistics
curl "http://localhost:8080/api/drivers/stats/location-updates?startTime=2024-01-01T00:00:00&endTime=2024-12-31T23:59:59"
```

## 🐛 Troubleshooting

### Common Issues

1. **Services Not Starting**
   ```bash
   # Check service health
   docker-compose ps
   
   # View logs
   docker-compose logs [service-name]
   ```

2. **Database Connection Issues**
   ```bash
   # Restart database
   docker-compose restart postgres
   
   # Check database logs
   docker-compose logs postgres
   ```

3. **High Memory Usage**
   ```bash
   # Monitor container resources
   docker stats
   
   # Adjust JVM settings in docker-compose.yml
   ```

### Health Checks

All services include health checks accessible via:
- Application: http://localhost:8080/api/actuator/health
- Database: Automatic via Docker health checks
- Redis: Automatic via Docker health checks
- Kafka: Automatic via Docker health checks

## 📚 API Documentation

Complete API documentation is available via Swagger UI at:
http://localhost:8080/api/swagger-ui.html

### Key API Endpoints

- **Restaurants**: `/api/restaurants/*` - Browse restaurants and menus
- **Orders**: `/api/orders/*` - Order management and tracking
- **Drivers**: `/api/drivers/*` - Driver management and location tracking
- **Real-time**: `/ws` - WebSocket connections for live updates

## 🔄 Development Workflow

### Local Development

#### Prerequisites for Local Development

Since the application now uses PostgreSQL for all environments, you need to set up PostgreSQL locally:

**Option 1: Using Docker (Recommended)**
```bash
# Start only the dependencies (PostgreSQL, Redis, Kafka)
docker-compose up -d postgres redis kafka
```

**Option 2: Local PostgreSQL Installation**
If you prefer to run PostgreSQL locally:
```bash
# Install PostgreSQL (MacOS with Homebrew)
brew install postgresql
brew services start postgresql

# Create database and user
createdb swifteats
createuser -s swifteats_user
psql -d swifteats -c "ALTER USER swifteats_user WITH PASSWORD 'swifteats_password';"
```

#### Running the Application

1. **Start Dependencies**
   ```bash
   docker-compose up -d postgres redis kafka
   ```

2. **Run Application Locally**
   ```bash
   # Default profile (uses PostgreSQL on localhost:5433)
   ./mvnw spring-boot:run
   
   # Or with specific profile
   ./mvnw spring-boot:run -Dspring.profiles.active=dev
   ```

3. **Run Tests**
   ```bash
   ./mvnw test
   ```

4. **Generate Coverage Report**
   ```bash
   ./mvnw jacoco:report
   open target/site/jacoco/index.html
   ```

### Building and Deployment

```bash
# Build application
./mvnw clean package

# Build Docker image
docker build -t swifteats-backend .

# Deploy with Docker Compose
docker-compose up -d --build
```

## 🎯 Performance Optimization

### Achieved Optimizations

1. **Caching Strategy**: Redis caching for frequently accessed data
2. **Async Processing**: Non-blocking payment and location processing
3. **Database Indexing**: Optimized queries for location-based searches
4. **Connection Pooling**: Efficient database connection management
5. **JVM Tuning**: G1GC with optimized heap settings

### Monitoring Performance

Use the integrated Grafana dashboards to monitor:
- Response time percentiles
- Cache hit rates
- Database query performance
- JVM metrics and garbage collection
- Location update throughput

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 📄 License

This project is part of the SwiftEats food delivery platform implementation.

---

For more detailed technical information, see:
- [ARCHITECTURE.md](ARCHITECTURE.md) - Detailed system design
- [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) - Code organization
- [API-SPECIFICATION.yml](API-SPECIFICATION.yml) - Complete API reference
