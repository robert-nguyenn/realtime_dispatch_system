-- ClickHouse initialization script for analytics

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS dispatch_analytics;

USE dispatch_analytics;

-- Ride events table for streaming analytics
CREATE TABLE ride_events (
    event_id UUID DEFAULT generateUUIDv4(),
    ride_id String,
    event_type Enum8('REQUESTED' = 1, 'MATCHED' = 2, 'ACCEPTED' = 3, 'STARTED' = 4, 'COMPLETED' = 5, 'CANCELLED' = 6),
    rider_id String,
    driver_id String,
    pickup_lat Float64,
    pickup_lng Float64,
    destination_lat Nullable(Float64),
    destination_lng Nullable(Float64),
    fare_amount Nullable(Decimal(10, 2)),
    estimated_duration_minutes Nullable(UInt32),
    actual_duration_minutes Nullable(UInt32),
    surge_multiplier Float32 DEFAULT 1.0,
    timestamp DateTime DEFAULT now(),
    processing_time DateTime DEFAULT now()
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(timestamp)
ORDER BY (timestamp, ride_id)
TTL timestamp + INTERVAL 1 YEAR;

-- Driver location tracking table
CREATE TABLE driver_locations (
    location_id UUID DEFAULT generateUUIDv4(),
    driver_id String,
    lat Float64,
    lng Float64,
    heading Nullable(Float32),
    speed_kmh Nullable(Float32),
    accuracy_meters Nullable(Float32),
    status Enum8('OFFLINE' = 1, 'AVAILABLE' = 2, 'BUSY' = 3, 'EN_ROUTE' = 4),
    timestamp DateTime DEFAULT now(),
    processing_time DateTime DEFAULT now()
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(timestamp)
ORDER BY (timestamp, driver_id)
TTL timestamp + INTERVAL 6 MONTH;

-- ETA calculations table
CREATE TABLE eta_calculations (
    calculation_id UUID DEFAULT generateUUIDv4(),
    ride_id String,
    driver_id String,
    pickup_eta_minutes UInt16,
    trip_eta_minutes UInt16,
    distance_km Float32,
    calculated_at DateTime DEFAULT now()
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(calculated_at)
ORDER BY (calculated_at, ride_id);

-- Surge pricing metrics
CREATE TABLE surge_metrics (
    metric_id UUID DEFAULT generateUUIDv4(),
    grid_cell String,
    active_rides UInt32,
    available_drivers UInt32,
    demand_supply_ratio Float32,
    surge_multiplier Float32,
    timestamp DateTime DEFAULT now()
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(timestamp)
ORDER BY (timestamp, grid_cell);

-- Materialized views for real-time analytics

-- Real-time ride metrics by hour
CREATE MATERIALIZED VIEW ride_metrics_hourly
ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(hour)
ORDER BY (hour, event_type)
AS SELECT
    toStartOfHour(timestamp) as hour,
    event_type,
    count() as event_count,
    avg(fare_amount) as avg_fare,
    avg(estimated_duration_minutes) as avg_duration
FROM ride_events
GROUP BY hour, event_type;

-- Driver utilization metrics
CREATE MATERIALIZED VIEW driver_utilization_hourly
ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(hour)
ORDER BY (hour, driver_id)
AS SELECT
    toStartOfHour(timestamp) as hour,
    driver_id,
    countIf(status = 'BUSY') as busy_count,
    countIf(status = 'AVAILABLE') as available_count,
    count() as total_updates
FROM driver_locations
GROUP BY hour, driver_id;

-- Grid-based demand heatmap (5-minute windows)
CREATE MATERIALIZED VIEW demand_heatmap_5min
ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(window_start)
ORDER BY (window_start, grid_cell)
AS SELECT
    toStartOfFiveMinute(timestamp) as window_start,
    concat(toString(round(pickup_lat, 2)), ',', toString(round(pickup_lng, 2))) as grid_cell,
    count() as ride_requests
FROM ride_events
WHERE event_type = 'REQUESTED'
GROUP BY window_start, grid_cell;
