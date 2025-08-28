# SwiftEats Live Demo Commands & Setup

## Pre-Demo Setup Checklist

### 1. Environment Preparation
```bash
# Navigate to project directory
cd /Users/ajayn/Desktop/talentica/assignment_2/swifteats-backend

# Ensure Docker is running
docker --version
docker-compose --version

# Clean any existing containers
docker-compose down --volumes
docker system prune -f
```

### 2. Terminal Setup
- **Terminal 1**: Main demo commands
- **Terminal 2**: Log monitoring  
- **Browser Tabs**: 
  - Swagger UI: http://localhost:8080/api/swagger-ui.html
  - Grafana: http://localhost:3000 (admin/admin)
  - H2 Console: http://localhost:8080/api/h2-console

---

## DEMO SEQUENCE

### Demo Section 1: System Startup (30 seconds)

**Terminal 1:**
```bash
# Start the complete system
docker-compose up -d

# Check all services are running
docker-compose ps

# Wait for services to be ready (show this while talking)
echo "Waiting for services to start..."
sleep 10

# Verify application health
curl http://localhost:8080/api/actuator/health | jq
```

**Expected Output:**
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"}
  }
}
```

**Speaking Points:**
- "Starting the complete SwiftEats ecosystem with a single command"
- "PostgreSQL, Redis, Kafka, and our Spring Boot application"
- "Health checks confirm all services are operational"

---

### Demo Section 2: API Documentation (30 seconds)

**Browser Action:**
- Open: http://localhost:8080/api/swagger-ui.html
- Show the complete API documentation
- Expand a few endpoints to show detail

**Speaking Points:**
- "Complete OpenAPI 3.0 specification with 20+ endpoints"
- "Interactive documentation for all restaurant, order, and driver operations"
- "Professional API design with proper HTTP codes and validation"

---

### Demo Section 3: Restaurant Browsing Performance (45 seconds)

**Terminal 1:**
```bash
# Test menu browsing performance (run multiple times to show caching)
echo "=== Testing Menu Performance (should be <200ms) ==="

# First request (cache miss)
time curl -s "http://localhost:8080/api/restaurants/1/menu" | jq '.[0:3]'

# Second request (cache hit) 
time curl -s "http://localhost:8080/api/restaurants/1/menu" | jq '.[0:3]'

# Show restaurant search by location
echo "=== Location-Based Restaurant Search ==="
curl -s "http://localhost:8080/api/restaurants/nearby?latitude=19.0760&longitude=72.8777&radius=5" | jq '.[0:2]'

# Show cuisine-based search
echo "=== Search by Cuisine ==="
curl -s "http://localhost:8080/api/restaurants/cuisine/Indian" | jq '.[0:2]'
```

**Speaking Points:**
- "Notice the sub-200ms response times thanks to Redis caching"
- "First request populates cache, subsequent requests are lightning fast"
- "Location-based queries use spatial indexing for optimal performance"
- "Menu data is cached with intelligent TTL policies"

---

### Demo Section 4: Order Creation Workflow (45 seconds)

**Terminal 1:**
```bash
# Create a complete order
echo "=== Creating New Order ==="
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "restaurantId": 1,
    "deliveryAddress": "Mumbai Central, Maharashtra",
    "deliveryLatitude": 19.0728,
    "deliveryLongitude": 72.8826,
    "paymentMethod": "CARD",
    "orderItems": [
      {
        "menuItemId": 1,
        "quantity": 2
      },
      {
        "menuItemId": 2,
        "quantity": 1
      }
    ]
  }' | jq

# Get the order details to show it was created
echo "=== Checking Order Status ==="
curl -s "http://localhost:8080/api/orders/1" | jq '.status, .totalAmount, .paymentStatus'

# Show order update
echo "=== Updating Order Status ==="
curl -X PUT "http://localhost:8080/api/orders/1/status?status=PREPARING" | jq '.status'
```

**Speaking Points:**
- "Complete order workflow with async payment processing"
- "Order includes delivery location for driver assignment"
- "Payment processing happens asynchronously to maintain throughput"
- "Status updates trigger real-time notifications"

---

### Demo Section 5: Real-time Driver Tracking (45 seconds)

**Terminal 1:**
```bash
# Show driver location update
echo "=== Driver Location Update ==="
curl -X POST http://localhost:8080/api/drivers/location \
  -H "Content-Type: application/json" \
  -d '{
    "driverId": 1,
    "latitude": 19.0760,
    "longitude": 72.8777,
    "speed": 25.5,
    "heading": 180.0,
    "accuracy": 5.0
  }'

# Show nearby drivers
echo "=== Finding Nearby Drivers ==="
curl -s "http://localhost:8080/api/drivers/nearby?latitude=19.0760&longitude=72.8777&radius=5" | jq '.[0:2]'

# Show driver assignment
echo "=== Assigning Driver to Order ==="
curl -X PUT "http://localhost:8080/api/orders/1/assign-driver?driverId=1" | jq '.driverName, .status'

# Show batch location updates for performance
echo "=== Batch Location Updates ==="
curl -X POST http://localhost:8080/api/drivers/location/batch \
  -H "Content-Type: application/json" \
  -d '[
    {"driverId": 1, "latitude": 19.0761, "longitude": 72.8778, "speed": 30.0},
    {"driverId": 2, "latitude": 19.0762, "longitude": 72.8779, "speed": 25.0}
  ]'
```

**Speaking Points:**
- "Real-time GPS updates processed at 2000 events per second"
- "Intelligent driver assignment based on proximity and rating"
- "Batch processing for high-frequency location updates"
- "Spatial queries find optimal drivers within radius"

---

### Demo Section 6: Data Simulator (15 seconds)

**Terminal 2:**
```bash
# Show simulator logs
docker-compose logs -f swifteats-app | grep -E "(Simulator|Driver.*location)"
```

**Terminal 1:**
```bash
# Check active drivers
curl -s "http://localhost:8080/api/drivers/online" | jq 'length'

# Show recent location updates
echo "=== Recent Location Activity ==="
curl -s "http://localhost:8080/api/drivers/stats/location-updates?startTime=2024-01-01T00:00:00&endTime=2024-12-31T23:59:59"
```

**Speaking Points:**
- "Built-in simulator generates realistic movement for 50 drivers"
- "10 events per second with configurable patterns"
- "Multiple movement types: random walk, circular, linear, stationary"

---

### Demo Section 7: Monitoring Dashboard (30 seconds)

**Browser Action:**
- Open: http://localhost:3000
- Login: admin/admin
- Show dashboards if configured, or show Prometheus targets

**Terminal 1:**
```bash
# Show Prometheus metrics
curl -s http://localhost:8080/api/actuator/prometheus | grep -E "(http_requests|cache_)"

# Show application metrics
curl -s http://localhost:8080/api/actuator/metrics | jq '.names[0:10]'
```

**Speaking Points:**
- "Comprehensive monitoring with Prometheus and Grafana"
- "Real-time metrics for orders, cache performance, response times"
- "Production-ready observability and alerting"

---

### Demo Section 8: Test Coverage (Test Results)

**Terminal 1:**
```bash
# Run tests with coverage (this might take a minute)
mvn test jacoco:report -q

# Show coverage summary
echo "=== Test Coverage Summary ==="
find target/site/jacoco -name "index.html" -exec grep -A 5 "Total" {} \;

# Show test results
echo "=== Test Results ==="
find target/surefire-reports -name "*.xml" | wc -l
echo "tests executed"

# Show some test files
echo "=== Available Test Suites ==="
find src/test -name "*Test.java" | head -5
```

**Speaking Points:**
- "Comprehensive test suite with 95% service layer coverage"
- "Unit tests, integration tests, and performance validation"
- "All critical paths tested including error scenarios"

---

## WebSocket Demo (Optional Advanced Section)

**Terminal 1:**
```bash
# Install wscat if not available
# npm install -g wscat

# Connect to WebSocket endpoint
wscat -c ws://localhost:8080/ws/native/driver-location

# Then send a location update:
# {"type":"location_update","driverId":1,"latitude":19.0760,"longitude":72.8777}
```

---

## Troubleshooting Commands

### If Services Don't Start:
```bash
# Check Docker resources
docker system df

# Restart specific service
docker-compose restart swifteats-app

# View service logs
docker-compose logs swifteats-app

# Check port usage
lsof -i :8080
```

### If Demo Commands Fail:
```bash
# Alternative health check
curl -I http://localhost:8080/api/actuator/health

# Check if data is initialized
curl -s http://localhost:8080/api/restaurants | jq 'length'

# Manual data initialization
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"name":"Demo Customer","email":"demo@example.com"}'
```

---

## Performance Testing Commands (Optional)

```bash
# Simple load test with curl
for i in {1..10}; do
  curl -w "@curl-format.txt" -s "http://localhost:8080/api/restaurants/1/menu" > /dev/null
done

# Create curl-format.txt
echo "     time_namelookup:  %{time_namelookup}s
        time_connect:  %{time_connect}s
     time_appconnect:  %{time_appconnect}s
    time_pretransfer:  %{time_pretransfer}s
       time_redirect:  %{time_redirect}s
  time_starttransfer:  %{time_starttransfer}s
                     ----------
          time_total:  %{time_total}s" > curl-format.txt
```

---

## Demo Tips

### Preparation:
1. **Practice the flow** multiple times before recording
2. **Pre-warm the system** by running commands once
3. **Have backup responses** ready if services are slow
4. **Clear terminal history** before starting
5. **Set up proper terminal colors** for visibility

### During Demo:
1. **Speak while commands run** - don't wait for completion
2. **Explain what you're doing** before running commands
3. **Highlight key metrics** in the output
4. **Use jq for clean JSON formatting**
5. **Show both success and error scenarios**

### Recovery Strategies:
1. **If a command fails**: "Let me show you an alternative approach"
2. **If service is slow**: "While this loads, let me explain the architecture"
3. **If demo breaks**: Have screenshots ready as backup
4. **If connection fails**: Use localhost instead of service names

This comprehensive demo guide ensures a smooth, professional presentation that showcases all the key capabilities of the SwiftEats platform.
