# Realtime Dispatch System - Build and Run Guide

## Prerequisites

- Java 21
- Rust (latest stable)
- Docker & Docker Compose
- Maven 3.8+
- Android Studio (for mobile apps)
- Node.js 18+ (for feature flags service)

## Quick Start

### 1. Start Infrastructure Services

```bash
# Start all infrastructure components
docker-compose up -d

# Wait for services to be ready (about 30-60 seconds)
docker-compose logs -f kafka clickhouse postgres
```

### 2. Build and Start Geo-Index Service (Rust)

```bash
cd geo-index-service

# Build the service
cargo build --release

# Run the service
cargo run --release
```

The geo-index service will be available at:
- gRPC: `localhost:50051`
- HTTP (metrics): `localhost:8080`

### 3. Build and Start Dispatch API (Java)

```bash
cd dispatch-api

# Generate protobuf classes
mvn compile

# Run the application
mvn spring-boot:run
```

The dispatch API will be available at:
- REST API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Actuator: `http://localhost:8080/actuator`

### 4. Build and Deploy Stream Processing Jobs

```bash
cd stream-processing

# Build Flink jobs
mvn clean package

# Submit jobs to Flink cluster
./submit-jobs.sh
```

Flink Web UI will be available at: `http://localhost:8081`

### 5. Build Android Applications

```bash
cd android-apps

# Build both driver and rider apps
./gradlew build

# Install on connected device/emulator
./gradlew installDebug
```

## Testing the System

### 1. Test Geo-Index Service

```bash
# Check health
curl http://localhost:8080/health

# Check metrics
curl http://localhost:8080/metrics

# Check stats
curl http://localhost:8080/stats
```

### 2. Test Dispatch API

```bash
# Create a ride
curl -X POST http://localhost:8080/api/rides \
  -H "Content-Type: application/json" \
  -d '{
    "riderId": "rider_001",
    "pickupLat": 40.7589,
    "pickupLng": -73.9851,
    "destinationLat": 40.7505,
    "destinationLng": -73.9934
  }'

# Update driver location
curl -X POST http://localhost:8080/api/drivers/driver_001/location \
  -H "Content-Type: application/json" \
  -d '{
    "driverId": "driver_001",
    "lat": 40.7589,
    "lng": -73.9851
  }'

# Set driver online
curl -X POST "http://localhost:8080/api/drivers/driver_001/online?lat=40.7589&lng=-73.9851"
```

### 3. Monitor with Grafana

1. Open Grafana: `http://localhost:3000`
2. Login: `admin/admin`
3. Import provided dashboards from `monitoring/grafana/dashboards/`

## Development Setup

### Environment Variables

Create `.env` files for each service:

**dispatch-api/.env**
```
SPRING_PROFILES_ACTIVE=dev
DATABASE_URL=jdbc:postgresql://localhost:5432/dispatch_db
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
GEO_INDEX_HOST=localhost
GEO_INDEX_PORT=50051
```

**geo-index-service/.env**
```
RUST_LOG=info
GRPC_PORT=50051
HTTP_PORT=8080
```

### IDE Setup

**IntelliJ IDEA (Java):**
1. Import `dispatch-api` as Maven project
2. Enable annotation processing for MapStruct
3. Install Protobuf plugin

**VS Code (Rust):**
1. Install Rust-analyzer extension
2. Install CodeLLDB for debugging

**Android Studio:**
1. Import `android-apps` project
2. Sync Gradle files
3. Enable Kotlin support

## Production Deployment

### Docker Images

Build production images:

```bash
# Build Dispatch API
cd dispatch-api
docker build -t dispatch-api:latest .

# Build Geo-Index Service  
cd geo-index-service
docker build -t geo-index-service:latest .

# Build Stream Processing
cd stream-processing
docker build -t stream-processing:latest .
```

### Kubernetes Deployment

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmaps/
kubectl apply -f k8s/secrets/
kubectl apply -f k8s/deployments/
kubectl apply -f k8s/services/
kubectl apply -f k8s/ingress/
```

## Monitoring & Observability

### Metrics

- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3000`

Key dashboards:
- System Overview
- API Performance
- Geo-Index Performance
- Stream Processing Metrics
- Mobile App Analytics

### Logs

```bash
# View all service logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f dispatch-api
docker-compose logs -f geo-index-service
docker-compose logs -f flink-jobmanager
```

### Tracing

OpenTelemetry traces are exported to Jaeger:
- Jaeger UI: `http://localhost:16686`

## Troubleshooting

### Common Issues

1. **Services not connecting to Kafka**
   - Check if Kafka is running: `docker-compose ps kafka`
   - Verify network connectivity: `docker-compose logs kafka`

2. **Geo-Index gRPC connection failed**
   - Check if service is running: `curl http://localhost:8080/health`
   - Verify port is not blocked by firewall

3. **Database connection issues**
   - Check PostgreSQL status: `docker-compose ps postgres`
   - Verify credentials in application.yml

4. **Android app network issues**
   - Use `10.0.2.2` instead of `localhost` for Android emulator
   - Check network permissions in AndroidManifest.xml

### Performance Tuning

**JVM Options for Dispatch API:**
```bash
export JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:+UseStringDeduplication"
```

**Rust Optimization:**
```toml
[profile.release]
lto = true
codegen-units = 1
panic = "abort"
```

**Kafka Tuning:**
```properties
# In docker-compose.yml
KAFKA_NUM_PARTITIONS=6
KAFKA_DEFAULT_REPLICATION_FACTOR=1
KAFKA_LOG_RETENTION_HOURS=24
```

## API Documentation

- **REST API**: Available at `/swagger-ui.html` when Dispatch API is running
- **gRPC API**: Protocol buffer definitions in `proto/geoindex.proto`

## Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature-name`
3. Make changes and add tests
4. Run full test suite: `./scripts/run-tests.sh`
5. Submit pull request

## License

MIT License - see LICENSE file for details.
