# 🚀 Realtime Dispatch System - Development & Deployment Guide

## Table of Contents
1. [Quick Start](#quick-start)
2. [Development Setup](#development-setup)
3. [System Architecture](#system-architecture)
4. [API Documentation](#api-documentation)
5. [Testing](#testing)
6. [Monitoring](#monitoring)
7. [Production Deployment](#production-deployment)
8. [Troubleshooting](#troubleshooting)

## Quick Start

### Prerequisites
- Docker Desktop with Docker Compose
- Java 21 JDK
- Rust (1.70+)
- Node.js & npm (for Android development)
- Android Studio (for mobile apps)

### 1. Start Infrastructure
```bash
# Start all services
docker-compose up -d

# Check service health
docker-compose ps

# View logs
docker-compose logs -f
```

### 2. Build and Run Services

#### Java Dispatch API
```bash
cd dispatch-api
./gradlew clean build
./gradlew bootRun
```

#### Rust Geo-Index Service
```bash
cd geo-index-service
cargo build --release
cargo run --release
```

#### Submit Flink Jobs
```bash
cd stream-processing
chmod +x submit-jobs.sh
./submit-jobs.sh
```

### 3. Test the System
```bash
cd scripts
chmod +x test-system.sh
./test-system.sh
```

### 4. Access Services
- **Dispatch API**: http://localhost:8080/swagger-ui/index.html
- **Geo-Index Service**: http://localhost:8081
- **Flink Dashboard**: http://localhost:8082
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **ClickHouse**: http://localhost:8123 (playground)

## Development Setup

### Environment Variables
Create `.env` file:
```bash
# Database
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=dispatch_system
POSTGRES_USER=dispatch_user
POSTGRES_PASSWORD=dispatch_password

# ClickHouse
CLICKHOUSE_HOST=localhost
CLICKHOUSE_PORT=8123
CLICKHOUSE_DB=analytics

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Services
GEO_INDEX_SERVICE_URL=http://localhost:8081
FLINK_JOBMANAGER_URL=localhost:8082

# Security
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000
```

### IDE Setup

#### IntelliJ IDEA (Java)
1. Import `dispatch-api` as Gradle project
2. Set Java 21 as project SDK
3. Enable annotation processing for MapStruct
4. Install plugins: Spring Boot, Lombok

#### VS Code (Rust)
1. Install Rust extensions
2. Configure `settings.json`:
```json
{
    "rust-analyzer.cargo.features": "all",
    "rust-analyzer.checkOnSave.command": "clippy"
}
```

#### Android Studio
1. Import `android-apps/driver-app`
2. Sync Gradle
3. Set up emulator or device

### Database Setup

#### PostgreSQL Initialization
```sql
-- Run in PostgreSQL
\i scripts/init-postgres.sql
```

#### ClickHouse Initialization
```sql
-- Run in ClickHouse
SOURCE scripts/init-clickhouse.sql;
```

## System Architecture

### Components Overview
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Android Apps   │    │  Dispatch API   │    │ Geo-Index Svc  │
│  (Kotlin/Compose│◄──►│ (Spring Boot)   │◄──►│    (Rust)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       ▼                       │
         │              ┌─────────────────┐              │
         └─────────────►│     Kafka       │◄─────────────┘
                        │  (Event Stream) │
                        └─────────────────┘
                                 │
                                 ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   PostgreSQL    │    │  Flink Jobs     │    │   ClickHouse    │
│  (Operational)  │    │ (Stream Proc.)  │    │  (Analytics)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         └─────────────────────┬─┴───────────────────────┘
                               │
                               ▼
                    ┌─────────────────┐
                    │   Monitoring    │
                    │ (Grafana/Prom.) │
                    └─────────────────┘
```

### Data Flow
1. **Driver Location Updates**: Android → Dispatch API → Geo-Index Service
2. **Ride Requests**: Android → Dispatch API → Driver Assignment Algorithm
3. **Real-time Events**: All services → Kafka → Flink → ClickHouse
4. **Monitoring**: All services → Prometheus → Grafana

### Key Algorithms

#### Driver Assignment
```java
// Weighted scoring algorithm
score = (1 / distance) * 0.4 +  
        (driver.rating / 5.0) * 0.3 + 
        (driver.acceptanceRate) * 0.2 + 
        (availabilityBonus) * 0.1
```

#### Geohash Spatial Indexing
```rust
// Geohash precision levels
precision_9 = ~4.8m x 4.8m    // for exact matching
precision_7 = ~76m x 76m      // for nearby search
precision_5 = ~2.4km x 1.2km  // for area coverage
```

#### ETA Calculation
```sql
-- Flink SQL for real-time ETA
SELECT 
    ride_id,
    AVG(historical_duration) * traffic_factor as estimated_eta
FROM ride_events
WHERE pickup_area = current_pickup_area
WINDOW TUMBLING(INTERVAL '1' MINUTE)
```

## API Documentation

### Driver Endpoints

#### Set Driver Online
```http
POST /api/drivers/{driverId}/online?lat={lat}&lng={lng}
```

#### Update Location
```http
POST /api/drivers/{driverId}/location
Content-Type: application/json

{
  "driverId": "driver_001",
  "lat": 40.7589,
  "lng": -73.9851,
  "heading": 45,
  "speedKmh": 30.5,
  "accuracyMeters": 5.0
}
```

#### Get Driver Info
```http
GET /api/drivers/{driverId}
```

### Ride Endpoints

#### Create Ride Request
```http
POST /api/rides
Content-Type: application/json

{
  "riderId": "rider_001",
  "pickupLat": 40.7589,
  "pickupLng": -73.9851,
  "destinationLat": 40.7505,
  "destinationLng": -73.9934
}
```

#### Get Ride Status
```http
GET /api/rides/{rideId}
```

#### Driver Actions
```http
POST /api/rides/{rideId}/accept?driverId={driverId}
POST /api/rides/{rideId}/start?driverId={driverId}
POST /api/rides/{rideId}/complete?driverId={driverId}&fareAmount={amount}
```

### Geo-Index Service

#### Find Nearby Drivers
```http
GET /geo-index/drivers/nearby?lat={lat}&lng={lng}&radius={meters}&limit={count}
```

#### Service Stats
```http
GET /stats
```

## Testing

### Unit Tests
```bash
# Java tests
cd dispatch-api
./gradlew test

# Rust tests
cd geo-index-service
cargo test

# Android tests
cd android-apps/driver-app
./gradlew test
```

### Integration Tests
```bash
# Start test environment
docker-compose -f docker-compose.test.yml up -d

# Run integration tests
./scripts/test-system.sh
```

### Load Testing
```bash
# Install k6
# Run load test
k6 run scripts/load-test.js
```

### Sample Load Test Script
```javascript
// scripts/load-test.js
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  vus: 50, // Virtual users
  duration: '5m',
};

export default function() {
  // Test driver location update
  let locationData = {
    driverId: `driver_${Math.floor(Math.random() * 100)}`,
    lat: 40.7589 + (Math.random() - 0.5) * 0.01,
    lng: -73.9851 + (Math.random() - 0.5) * 0.01,
  };

  let response = http.post(
    'http://localhost:8080/api/drivers/driver_001/location',
    JSON.stringify(locationData),
    { headers: { 'Content-Type': 'application/json' } }
  );

  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });
}
```

## Monitoring

### Grafana Dashboards

#### System Overview
- **Rides per minute**: Real-time ride creation rate
- **Active drivers**: Currently online drivers
- **Assignment success rate**: % of rides successfully assigned
- **API response times**: P50, P95, P99 latencies

#### Driver Performance
- **Location update frequency**: Updates per driver per minute
- **Driver utilization**: % time with active rides
- **Average earnings**: Per driver per hour

#### Operational Metrics
- **Database connections**: PostgreSQL pool usage
- **Kafka lag**: Consumer group lag by topic
- **Memory usage**: JVM heap, Rust memory
- **Error rates**: HTTP 4xx/5xx by endpoint

### Custom Metrics
```java
// Java - Custom metrics
@Component
public class DispatchMetrics {
    private final Counter ridesCreated = Counter.build()
        .name("rides_created_total")
        .help("Total rides created")
        .register();
        
    private final Histogram assignmentLatency = Histogram.build()
        .name("assignment_latency_seconds")
        .help("Driver assignment latency")
        .register();
}
```

```rust
// Rust - Custom metrics
lazy_static! {
    static ref LOCATION_UPDATES: Counter = register_counter!(
        "location_updates_total", 
        "Total driver location updates"
    ).unwrap();
    
    static ref SPATIAL_QUERY_TIME: Histogram = register_histogram!(
        "spatial_query_duration_seconds",
        "Spatial query execution time"
    ).unwrap();
}
```

### Alerts Configuration
```yaml
# prometheus/alerts.yml
groups:
- name: dispatch_system
  rules:
  - alert: HighAssignmentLatency
    expr: histogram_quantile(0.95, assignment_latency_seconds) > 5
    for: 2m
    labels:
      severity: warning
    annotations:
      summary: "Assignment latency is high"
      
  - alert: LowDriverAvailability
    expr: active_drivers < 10
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "Very few drivers available"
```

## Production Deployment

### Docker Deployment
```bash
# Build production images
docker-compose -f docker-compose.prod.yml build

# Deploy with resource limits
docker-compose -f docker-compose.prod.yml up -d
```

### Kubernetes Deployment
```yaml
# k8s/dispatch-api-deployment.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: dispatch-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: dispatch-api
  template:
    metadata:
      labels:
        app: dispatch-api
    spec:
      containers:
      - name: dispatch-api
        image: dispatch-api:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

### Environment-specific Configurations

#### Development
```yaml
# config/application-dev.yml
server:
  port: 8080
logging:
  level:
    com.dispatch: DEBUG
spring:
  jpa:
    show-sql: true
```

#### Production
```yaml
# config/application-prod.yml
server:
  port: 8080
logging:
  level:
    root: INFO
spring:
  jpa:
    show-sql: false
  datasource:
    hikari:
      maximum-pool-size: 20
```

### Performance Tuning

#### JVM Options
```bash
# For Java service
-Xms2g -Xmx4g 
-XX:+UseG1GC 
-XX:MaxGCPauseMillis=200
-XX:+HeapDumpOnOutOfMemoryError
```

#### Rust Optimizations
```toml
# Cargo.toml for production builds
[profile.release]
opt-level = 3
lto = true
codegen-units = 1
panic = 'abort'
```

## Troubleshooting

### Common Issues

#### Service Won't Start
```bash
# Check logs
docker-compose logs [service-name]

# Check port conflicts
netstat -tulpn | grep :[port]

# Restart specific service
docker-compose restart [service-name]
```

#### Database Connection Issues
```bash
# Test PostgreSQL connection
docker exec -it postgres psql -U dispatch_user -d dispatch_system

# Check ClickHouse
curl http://localhost:8123/ping
```

#### High Memory Usage
```bash
# Check container memory
docker stats

# Java heap dump
docker exec dispatch-api jcmd 1 GC.run_finalization
docker exec dispatch-api jcmd 1 VM.gc

# Rust memory profiling
# Use valgrind or heaptrack
```

#### Kafka Issues
```bash
# List topics
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Check consumer lag
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --all-groups
```

### Performance Issues

#### Slow API Responses
1. Check database query performance
2. Monitor connection pool usage
3. Verify GC behavior
4. Check for lock contention

#### High Spatial Query Latency
1. Verify geohash precision settings
2. Check spatial index rebuild
3. Monitor memory usage
4. Tune grid bucket sizes

### Debug Commands
```bash
# Service health checks
curl http://localhost:8080/actuator/health
curl http://localhost:8081/health

# Metrics
curl http://localhost:8080/actuator/prometheus
curl http://localhost:8081/metrics

# Database queries
psql -U dispatch_user -d dispatch_system -c "SELECT COUNT(*) FROM rides;"
clickhouse-client --query "SELECT COUNT(*) FROM analytics.ride_events"
```

### Support and Maintenance

#### Log Aggregation
- Configure centralized logging (ELK stack)
- Set up log rotation
- Monitor error patterns

#### Backup Strategy
- PostgreSQL: Daily pg_dump
- ClickHouse: Partitioned backup
- Configuration: Git-based versioning

#### Monitoring Checklist
- [ ] All services responding to health checks
- [ ] Database connections stable
- [ ] Kafka consumers processing without lag  
- [ ] Memory and CPU usage within limits
- [ ] Error rates below threshold
- [ ] Assignment latency acceptable

For additional support, check the project wiki or create an issue in the repository.
