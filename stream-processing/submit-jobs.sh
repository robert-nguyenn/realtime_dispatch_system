#!/bin/bash

# Submit Flink Jobs Script
# This script submits stream processing jobs to the Flink cluster

set -e

FLINK_HOME=${FLINK_HOME:-/opt/flink}
JAR_FILE="target/stream-processing-1.0.0.jar"

echo "=== Submitting Dispatch Stream Processing Jobs ==="

# Check if Flink cluster is running
if ! curl -s http://localhost:8081/overview > /dev/null; then
    echo "Error: Flink cluster is not running. Please start it first."
    echo "Run: docker-compose up -d flink-jobmanager flink-taskmanager"
    exit 1
fi

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found. Building project..."
    mvn clean package -DskipTests
fi

echo "Submitting ETA Calculation and Surge Pricing Job..."

# Submit the main job
$FLINK_HOME/bin/flink run \
    --class com.dispatch.streaming.ETACalculationJob \
    --parallelism 2 \
    --detached \
    $JAR_FILE

echo "Job submitted successfully!"

# Wait a moment for job to start
sleep 5

# Show running jobs
echo "=== Current Running Jobs ==="
curl -s http://localhost:8081/jobs | jq '.jobs[] | {id: .id, name: .name, status: .status}'

echo ""
echo "Flink Web UI: http://localhost:8081"
echo "Job submission completed!"
