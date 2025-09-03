package com.dispatch.streaming;

import com.dispatch.streaming.function.ETACalculationFunction;
import com.dispatch.streaming.function.SurgeMetricsFunction;
import com.dispatch.streaming.model.DriverLocationEvent;
import com.dispatch.streaming.model.RideEvent;
import com.dispatch.streaming.schema.RideEventDeserializer;
import com.dispatch.streaming.schema.DriverLocationEventDeserializer;
import com.dispatch.streaming.sink.ClickHouseSink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;

/**
 * Flink job for real-time ETA calculation and surge pricing metrics
 */
public class ETACalculationJob {
    
    private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String RIDE_EVENTS_TOPIC = "ride-events";
    private static final String DRIVER_LOCATIONS_TOPIC = "driver-locations";
    
    public static void main(String[] args) throws Exception {
        // Set up the execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // Enable checkpointing for fault tolerance
        env.enableCheckpointing(60000); // 1 minute
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(30000);
        env.getCheckpointConfig().setCheckpointTimeout(600000); // 10 minutes
        
        // Configure parallelism
        env.setParallelism(2);
        
        // Create Kafka sources
        KafkaSource<RideEvent> rideEventSource = KafkaSource.<RideEvent>builder()
                .setBootstrapServers(KAFKA_BOOTSTRAP_SERVERS)
                .setTopics(RIDE_EVENTS_TOPIC)
                .setGroupId("eta-calculation-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new RideEventDeserializer())
                .build();
        
        KafkaSource<DriverLocationEvent> driverLocationSource = KafkaSource.<DriverLocationEvent>builder()
                .setBootstrapServers(KAFKA_BOOTSTRAP_SERVERS)
                .setTopics(DRIVER_LOCATIONS_TOPIC)
                .setGroupId("eta-calculation-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new DriverLocationEventDeserializer())
                .build();
        
        // Create data streams with watermarks
        DataStream<RideEvent> rideEvents = env
                .fromSource(rideEventSource, WatermarkStrategy
                        .<RideEvent>forBoundedOutOfOrderness(Duration.ofSeconds(30))
                        .withTimestampAssigner((event, timestamp) -> 
                            event.getTimestamp().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()),
                    "ride-events-source");
        
        DataStream<DriverLocationEvent> driverLocations = env
                .fromSource(driverLocationSource, WatermarkStrategy
                        .<DriverLocationEvent>forBoundedOutOfOrderness(Duration.ofSeconds(10))
                        .withTimestampAssigner((event, timestamp) -> 
                            event.getTimestamp().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()),
                    "driver-locations-source");
        
        // Stream 1: ETA Calculations
        // Join ride requests with driver locations to calculate ETAs
        DataStream<ETACalculationFunction.ETAResult> etaResults = rideEvents
                .filter(event -> "REQUESTED".equals(event.getEventType()))
                .connect(driverLocations.filter(event -> "AVAILABLE".equals(event.getStatus())))
                .process(new ETACalculationFunction())
                .name("eta-calculation");
        
        // Stream 2: Surge Pricing Metrics
        // Calculate demand/supply ratios for surge pricing in 5-minute windows
        DataStream<SurgeMetricsFunction.SurgeMetrics> surgeMetrics = rideEvents
                .filter(event -> "REQUESTED".equals(event.getEventType()))
                .keyBy(event -> getGridCell(event.getPickupLat(), event.getPickupLng()))
                .window(TumblingProcessingTimeWindows.of(Time.minutes(5)))
                .process(new SurgeMetricsFunction())
                .name("surge-metrics");
        
        // Stream 3: Real-time Analytics
        // Aggregate ride metrics by hour for real-time dashboards
        DataStream<RideEvent> hourlyMetrics = rideEvents
                .keyBy(RideEvent::getEventType)
                .window(TumblingProcessingTimeWindows.of(Time.hours(1)))
                .aggregate(new com.dispatch.streaming.function.RideMetricsAggregator())
                .name("hourly-metrics");
        
        // Sinks: Write results to ClickHouse
        etaResults.addSink(new ClickHouseSink.ETASink()).name("eta-clickhouse-sink");
        surgeMetrics.addSink(new ClickHouseSink.SurgeSink()).name("surge-clickhouse-sink");
        hourlyMetrics.addSink(new ClickHouseSink.RideEventSink()).name("ride-events-clickhouse-sink");
        
        // Write driver locations to ClickHouse for analytics
        driverLocations.addSink(new ClickHouseSink.DriverLocationSink()).name("driver-locations-clickhouse-sink");
        
        // Execute the job
        env.execute("ETA Calculation and Surge Pricing Job");
    }
    
    /**
     * Convert lat/lng to grid cell for grouping
     */
    private static String getGridCell(Double lat, Double lng) {
        if (lat == null || lng == null) {
            return "unknown";
        }
        // Create ~1km grid cells
        double gridSize = 0.01; // approximately 1km
        long latGrid = Math.round(lat / gridSize);
        long lngGrid = Math.round(lng / gridSize);
        return latGrid + "," + lngGrid;
    }
}
