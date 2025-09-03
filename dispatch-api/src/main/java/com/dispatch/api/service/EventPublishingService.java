package com.dispatch.api.service;

import com.dispatch.api.dto.events.DriverLocationEvent;
import com.dispatch.api.dto.events.RideEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EventPublishingService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventPublishingService.class);
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${app.kafka.topics.ride-events}")
    private String rideEventsTopic;
    
    @Value("${app.kafka.topics.driver-locations}")
    private String driverLocationsTopic;
    
    @Value("${app.kafka.topics.ride-assignments}")
    private String rideAssignmentsTopic;
    
    public EventPublishingService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void publishRideEvent(RideEvent event) {
        try {
            String key = event.getRideId().toString();
            
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(rideEventsTopic, key, event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.debug("Published ride event: {} for ride: {} with offset: {}", 
                               event.getEventType(), event.getRideId(), result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish ride event: {} for ride: {}", 
                               event.getEventType(), event.getRideId(), ex);
                }
            });
            
        } catch (Exception e) {
            logger.error("Error publishing ride event: {}", event.getEventType(), e);
        }
    }
    
    public void publishDriverLocationEvent(DriverLocationEvent event) {
        try {
            String key = event.getDriverId();
            
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(driverLocationsTopic, key, event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.debug("Published driver location event for driver: {} with offset: {}", 
                               event.getDriverId(), result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish driver location event for driver: {}", 
                               event.getDriverId(), ex);
                }
            });
            
        } catch (Exception e) {
            logger.error("Error publishing driver location event for driver: {}", event.getDriverId(), e);
        }
    }
    
    public void publishRideAssignmentEvent(String rideId, String driverId, String eventType) {
        try {
            RideAssignmentEvent assignmentEvent = new RideAssignmentEvent(rideId, driverId, eventType);
            String key = rideId;
            
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(rideAssignmentsTopic, key, assignmentEvent);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.debug("Published ride assignment event: {} for ride: {} and driver: {} with offset: {}", 
                               eventType, rideId, driverId, result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish ride assignment event: {} for ride: {} and driver: {}", 
                               eventType, rideId, driverId, ex);
                }
            });
            
        } catch (Exception e) {
            logger.error("Error publishing ride assignment event: {} for ride: {} and driver: {}", 
                        eventType, rideId, driverId, e);
        }
    }
    
    // Inner class for ride assignment events
    public static class RideAssignmentEvent {
        private String rideId;
        private String driverId;
        private String eventType;
        private long timestamp;
        
        public RideAssignmentEvent() {
            this.timestamp = System.currentTimeMillis();
        }
        
        public RideAssignmentEvent(String rideId, String driverId, String eventType) {
            this();
            this.rideId = rideId;
            this.driverId = driverId;
            this.eventType = eventType;
        }
        
        // Getters and setters
        public String getRideId() { return rideId; }
        public void setRideId(String rideId) { this.rideId = rideId; }
        
        public String getDriverId() { return driverId; }
        public void setDriverId(String driverId) { this.driverId = driverId; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
