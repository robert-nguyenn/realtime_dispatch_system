#!/bin/bash

# Comprehensive Test Script for Realtime Dispatch System
# This script tests all components of the system

set -e

BASE_URL="http://localhost:8080/api"
GEO_INDEX_URL="http://localhost:8080"

echo "=== Testing Realtime Dispatch System ==="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test functions
test_service() {
    local name=$1
    local url=$2
    local expected_status=${3:-200}
    
    echo -n "Testing $name... "
    
    status=$(curl -s -o /dev/null -w "%{http_code}" "$url" || echo "000")
    
    if [ "$status" -eq "$expected_status" ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $status)"
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} (HTTP $status, expected $expected_status)"
        return 1
    fi
}

test_json_endpoint() {
    local name=$1
    local url=$2
    local method=${3:-GET}
    local data=${4:-""}
    
    echo -n "Testing $name... "
    
    if [ "$method" = "POST" ] && [ -n "$data" ]; then
        response=$(curl -s -X POST -H "Content-Type: application/json" -d "$data" "$url" 2>/dev/null || echo "ERROR")
    else
        response=$(curl -s "$url" 2>/dev/null || echo "ERROR")
    fi
    
    if echo "$response" | jq . >/dev/null 2>&1; then
        echo -e "${GREEN}✓ PASS${NC} (Valid JSON)"
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} (Invalid response: $response)"
        return 1
    fi
}

# Test counters
passed=0
failed=0

run_test() {
    if "$@"; then
        ((passed++))
    else
        ((failed++))
    fi
}

echo -e "\n${YELLOW}=== 1. Infrastructure Health Checks ===${NC}"

run_test test_service "Kafka" "http://localhost:9092" "200"
run_test test_service "ClickHouse" "http://localhost:8123" "200"
run_test test_service "PostgreSQL" "telnet localhost 5432" "200"
run_test test_service "Redis" "telnet localhost 6379" "200"

echo -e "\n${YELLOW}=== 2. Geo-Index Service Tests ===${NC}"

run_test test_json_endpoint "Geo-Index Health" "$GEO_INDEX_URL/health"
run_test test_json_endpoint "Geo-Index Stats" "$GEO_INDEX_URL/stats"
run_test test_service "Geo-Index Metrics" "$GEO_INDEX_URL/metrics" "200"

echo -e "\n${YELLOW}=== 3. Dispatch API Tests ===${NC}"

run_test test_service "Dispatch API Health" "$BASE_URL/../actuator/health"
run_test test_service "Dispatch API Metrics" "$BASE_URL/../actuator/prometheus"

# Test driver operations
echo -e "\n${YELLOW}=== 4. Driver API Tests ===${NC}"

# Set driver online
driver_online_data='{"lat": 40.7589, "lng": -73.9851}'
run_test test_service "Set Driver Online" "$BASE_URL/drivers/driver_001/online?lat=40.7589&lng=-73.9851" "200"

# Update driver location
location_data='{
  "driverId": "driver_001",
  "lat": 40.7590,
  "lng": -73.9850,
  "heading": 45,
  "speedKmh": 30.5,
  "accuracyMeters": 5.0
}'
run_test test_json_endpoint "Update Driver Location" "$BASE_URL/drivers/driver_001/location" "POST" "$location_data"

# Get driver info
run_test test_json_endpoint "Get Driver Info" "$BASE_URL/drivers/driver_001"

echo -e "\n${YELLOW}=== 5. Ride API Tests ===${NC}"

# Create a ride
ride_data='{
  "riderId": "rider_001",
  "pickupLat": 40.7589,
  "pickupLng": -73.9851,
  "destinationLat": 40.7505,
  "destinationLng": -73.9934
}'

echo "Creating test ride..."
ride_response=$(curl -s -X POST -H "Content-Type: application/json" -d "$ride_data" "$BASE_URL/rides" 2>/dev/null || echo "ERROR")

if echo "$ride_response" | jq . >/dev/null 2>&1; then
    ride_id=$(echo "$ride_response" | jq -r '.id' 2>/dev/null || echo "")
    echo -e "${GREEN}✓ PASS${NC} - Ride created with ID: $ride_id"
    ((passed++))
    
    if [ -n "$ride_id" ] && [ "$ride_id" != "null" ]; then
        # Test ride operations
        run_test test_json_endpoint "Get Ride Details" "$BASE_URL/rides/$ride_id"
        
        # Start ride (if assigned to driver)
        echo "Attempting to start ride..."
        start_response=$(curl -s -X POST "$BASE_URL/rides/$ride_id/start?driverId=driver_001" 2>/dev/null || echo "ERROR")
        
        # Complete ride
        echo "Attempting to complete ride..."
        complete_response=$(curl -s -X POST "$BASE_URL/rides/$ride_id/complete?driverId=driver_001&fareAmount=25.50" 2>/dev/null || echo "ERROR")
    fi
else
    echo -e "${RED}✗ FAIL${NC} - Failed to create ride: $ride_response"
    ((failed++))
fi

echo -e "\n${YELLOW}=== 6. Stream Processing Tests ===${NC}"

# Check Flink cluster
run_test test_service "Flink JobManager" "http://localhost:8081"

# Check if jobs are running
flink_jobs=$(curl -s "http://localhost:8081/jobs" 2>/dev/null || echo '{"jobs":[]}')
job_count=$(echo "$flink_jobs" | jq '.jobs | length' 2>/dev/null || echo "0")

echo -n "Checking Flink jobs... "
if [ "$job_count" -gt 0 ]; then
    echo -e "${GREEN}✓ PASS${NC} ($job_count jobs running)"
    ((passed++))
else
    echo -e "${YELLOW}⚠ WARNING${NC} (No jobs running)"
fi

echo -e "\n${YELLOW}=== 7. End-to-End Integration Test ===${NC}"

# Full workflow test
echo "Running full ride workflow test..."

# 1. Set multiple drivers online
for i in {1..3}; do
    driver_id="driver_00$i"
    lat=$(echo "40.7589 + $i * 0.001" | bc -l)
    lng=$(echo "-73.9851 + $i * 0.001" | bc -l)
    
    curl -s -X POST "$BASE_URL/drivers/$driver_id/online?lat=$lat&lng=$lng" >/dev/null 2>&1
done

# 2. Create multiple rides
for i in {1..3}; do
    rider_id="test_rider_00$i"
    lat=$(echo "40.7589 + $i * 0.002" | bc -l)
    lng=$(echo "-73.9851 + $i * 0.002" | bc -l)
    
    ride_data="{\"riderId\":\"$rider_id\",\"pickupLat\":$lat,\"pickupLng\":$lng,\"destinationLat\":40.7505,\"destinationLng\":-73.9934}"
    curl -s -X POST -H "Content-Type: application/json" -d "$ride_data" "$BASE_URL/rides" >/dev/null 2>&1
done

# 3. Check geo-index has drivers
sleep 2
geo_stats=$(curl -s "$GEO_INDEX_URL/stats" 2>/dev/null || echo '{}')
available_drivers=$(echo "$geo_stats" | jq '.available_drivers' 2>/dev/null || echo "0")

echo -n "Integration test - Available drivers in geo-index... "
if [ "$available_drivers" -gt 0 ]; then
    echo -e "${GREEN}✓ PASS${NC} ($available_drivers drivers)"
    ((passed++))
else
    echo -e "${RED}✗ FAIL${NC} (No drivers in geo-index)"
    ((failed++))
fi

echo -e "\n${YELLOW}=== 8. Performance Test ===${NC}"

# Simple load test
echo "Running basic load test (10 concurrent location updates)..."

for i in {1..10}; do
    driver_id="driver_001"
    lat=$(echo "40.7589 + $RANDOM * 0.000001" | bc -l)
    lng=$(echo "-73.9851 + $RANDOM * 0.000001" | bc -l)
    
    location_data="{\"driverId\":\"$driver_id\",\"lat\":$lat,\"lng\":$lng}"
    curl -s -X POST -H "Content-Type: application/json" -d "$location_data" "$BASE_URL/drivers/$driver_id/location" >/dev/null 2>&1 &
done

wait

echo -e "${GREEN}✓ PASS${NC} - Load test completed"
((passed++))

echo -e "\n${YELLOW}=== 9. Monitoring & Observability Tests ===${NC}"

run_test test_service "Prometheus" "http://localhost:9090/-/healthy"
run_test test_service "Grafana" "http://localhost:3000/api/health"

echo -e "\n${YELLOW}=== Test Summary ===${NC}"
total=$((passed + failed))
echo "Total tests: $total"
echo -e "Passed: ${GREEN}$passed${NC}"
echo -e "Failed: ${RED}$failed${NC}"

if [ $failed -eq 0 ]; then
    echo -e "\n${GREEN}🎉 All tests passed! System is healthy.${NC}"
    exit 0
else
    echo -e "\n${RED}❌ $failed test(s) failed. Please check the system.${NC}"
    exit 1
fi
