package com.dispatch.api.service;

import com.dispatch.api.dto.events.RideEvent;
import com.dispatch.api.dto.websocket.DriverLocationUpdate;
import com.dispatch.api.dto.websocket.RideStatusUpdate;
import com.dispatch.api.dto.websocket.SurgeUpdate;
import com.dispatch.api.model.Driver;
import com.dispatch.api.model.Ride;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Real-time notification service using WebSocket
 * Handles driver location updates, ride status changes, surge pricing notifications
 */
@Service
public class RealtimeNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(RealtimeNotificationService.class);
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    
    public RealtimeNotificationService(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Notify rider about driver location updates during ride
     */
    public void notifyRiderOfDriverLocation(String riderId, String driverId, 
                                           double lat, double lng, double heading) {
        try {
            DriverLocationUpdate update = new DriverLocationUpdate(
                driverId, lat, lng, heading, LocalDateTime.now()
            );
            
            messagingTemplate.convertAndSendToUser(
                riderId, 
                "/queue/driver-location", 
                update
            );
            
            logger.debug("Sent driver location update to rider {}: driver {} at ({}, {})", 
                riderId, driverId, lat, lng);
                
        } catch (Exception e) {
            logger.error("Failed to send driver location update to rider {}", riderId, e);
        }
    }
    
    /**
     * Notify driver about ride requests
     */
    public void notifyDriverOfRideRequest(String driverId, Ride ride, int estimatedArrivalMinutes) {
        try {
            Map<String, Object> notification = Map.of(
                "type", "NEW_RIDE_REQUEST",
                "rideId", ride.getId().toString(),
                "pickupLocation", Map.of(
                    "lat", ride.getPickupLat(),
                    "lng", ride.getPickupLng()
                ),
                "destinationLocation", Map.of(
                    "lat", ride.getDestinationLat(),
                    "lng", ride.getDestinationLng()
                ),
                "estimatedFare", ride.getEstimatedFare(),
                "estimatedArrival", estimatedArrivalMinutes,
                "expiresAt", LocalDateTime.now().plusMinutes(2) // 2 minute timeout
            );
            
            messagingTemplate.convertAndSendToUser(
                driverId,
                "/queue/ride-requests",
                notification
            );
            
            logger.info("Sent ride request notification to driver {}: ride {}", driverId, ride.getId());
            
        } catch (Exception e) {
            logger.error("Failed to send ride request to driver {}", driverId, e);
        }
    }
    
    /**
     * Notify rider about ride status changes
     */
    public void notifyRiderOfRideStatus(String riderId, RideStatusUpdate statusUpdate) {
        try {
            messagingTemplate.convertAndSendToUser(
                riderId,
                "/queue/ride-status",
                statusUpdate
            );
            
            logger.debug("Sent ride status update to rider {}: {}", riderId, statusUpdate.getStatus());
            
        } catch (Exception e) {
            logger.error("Failed to send ride status update to rider {}", riderId, e);
        }
    }
    
    /**
     * Notify drivers about surge pricing changes in their area
     */
    public void notifySurgeUpdate(double lat, double lng, double radiusKm, BigDecimal surgeMultiplier) {
        try {
            SurgeUpdate surgeUpdate = new SurgeUpdate(
                lat, lng, radiusKm, surgeMultiplier, LocalDateTime.now()
            );
            
            // Broadcast to all drivers in the area (simplified - in production would target specific drivers)
            messagingTemplate.convertAndSend(
                "/topic/surge-updates",
                surgeUpdate
            );
            
            logger.debug("Broadcast surge update for area ({}, {}) with {}km radius: {}x", 
                lat, lng, radiusKm, surgeMultiplier);
                
        } catch (Exception e) {
            logger.error("Failed to broadcast surge update for area ({}, {})", lat, lng, e);
        }
    }
    
    /**
     * Notify all users about system-wide announcements
     */
    public void broadcastSystemAnnouncement(String message, String level) {
        try {
            Map<String, Object> announcement = Map.of(
                "type", "SYSTEM_ANNOUNCEMENT",
                "message", message,
                "level", level, // INFO, WARNING, CRITICAL
                "timestamp", LocalDateTime.now()
            );
            
            messagingTemplate.convertAndSend("/topic/announcements", announcement);
            
            logger.info("Broadcast system announcement: {} ({})", message, level);
            
        } catch (Exception e) {
            logger.error("Failed to broadcast system announcement", e);
        }
    }
    
    /**
     * Handle ride events and send appropriate notifications
     */
    public void handleRideEvent(RideEvent event) {
        try {
            switch (event.getEventType()) {
                case RIDE_CREATED -> handleRideCreated(event);
                case RIDE_ASSIGNED -> handleRideAssigned(event);
                case RIDE_STARTED -> handleRideStarted(event);
                case RIDE_COMPLETED -> handleRideCompleted(event);
                case RIDE_CANCELLED -> handleRideCancelled(event);
                case DRIVER_ARRIVED -> handleDriverArrived(event);
                default -> logger.debug("No notification handler for event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            logger.error("Failed to handle ride event: {}", event, e);
        }
    }
    
    private void handleRideCreated(RideEvent event) {
        // Notify nearby drivers about new ride request
        logger.info("New ride created: {}", event.getRideId());
    }
    
    private void handleRideAssigned(RideEvent event) {
        // Notify rider that driver has been assigned
        RideStatusUpdate update = new RideStatusUpdate(
            event.getRideId().toString(),
            "ASSIGNED",
            "Driver assigned and on the way",
            Map.of("driverId", event.getDriverId())
        );
        
        notifyRiderOfRideStatus(event.getRiderId(), update);
    }
    
    private void handleRideStarted(RideEvent event) {
        // Notify rider that ride has started
        RideStatusUpdate update = new RideStatusUpdate(
            event.getRideId().toString(),
            "IN_PROGRESS",
            "Your ride has started",
            Map.of("startTime", LocalDateTime.now())
        );
        
        notifyRiderOfRideStatus(event.getRiderId(), update);
    }
    
    private void handleRideCompleted(RideEvent event) {
        // Notify rider that ride is completed
        Map<String, Object> details = new HashMap<>();
        details.put("fareAmount", event.getFareAmount());
        details.put("duration", event.getDurationMinutes());
        details.put("completedAt", LocalDateTime.now());
        
        RideStatusUpdate update = new RideStatusUpdate(
            event.getRideId().toString(),
            "COMPLETED",
            "Your ride has been completed. Thank you!",
            details
        );
        
        notifyRiderOfRideStatus(event.getRiderId(), update);
    }
    
    private void handleRideCancelled(RideEvent event) {
        // Notify both rider and driver about cancellation
        RideStatusUpdate update = new RideStatusUpdate(
            event.getRideId().toString(),
            "CANCELLED",
            "Your ride has been cancelled",
            Map.of("cancelledAt", LocalDateTime.now())
        );
        
        notifyRiderOfRideStatus(event.getRiderId(), update);
        
        if (event.getDriverId() != null) {
            Map<String, Object> driverNotification = Map.of(
                "type", "RIDE_CANCELLED",
                "rideId", event.getRideId().toString(),
                "message", "Ride has been cancelled"
            );
            
            messagingTemplate.convertAndSendToUser(
                event.getDriverId(),
                "/queue/notifications",
                driverNotification
            );
        }
    }
    
    private void handleDriverArrived(RideEvent event) {
        // Notify rider that driver has arrived
        RideStatusUpdate update = new RideStatusUpdate(
            event.getRideId().toString(),
            "DRIVER_ARRIVED",
            "Your driver has arrived",
            Map.of("arrivedAt", LocalDateTime.now())
        );
        
        notifyRiderOfRideStatus(event.getRiderId(), update);
    }
    
    /**
     * Send driver performance metrics update
     */
    public void notifyDriverPerformanceUpdate(String driverId, Map<String, Object> metrics) {
        try {
            Map<String, Object> notification = Map.of(
                "type", "PERFORMANCE_UPDATE",
                "metrics", metrics,
                "timestamp", LocalDateTime.now()
            );
            
            messagingTemplate.convertAndSendToUser(
                driverId,
                "/queue/performance",
                notification
            );
            
            logger.debug("Sent performance update to driver {}", driverId);
            
        } catch (Exception e) {
            logger.error("Failed to send performance update to driver {}", driverId, e);
        }
    }
    
    /**
     * Send area demand forecast to drivers
     */
    public void notifyDemandForecast(String driverId, Map<String, Object> forecast) {
        try {
            Map<String, Object> notification = Map.of(
                "type", "DEMAND_FORECAST",
                "forecast", forecast,
                "timestamp", LocalDateTime.now()
            );
            
            messagingTemplate.convertAndSendToUser(
                driverId,
                "/queue/forecasts",
                notification
            );
            
            logger.debug("Sent demand forecast to driver {}", driverId);
            
        } catch (Exception e) {
            logger.error("Failed to send demand forecast to driver {}", driverId, e);
        }
    }
    
    /**
     * Test WebSocket connection
     */
    public void sendTestMessage(String userId, String message) {
        try {
            Map<String, Object> testMessage = Map.of(
                "type", "TEST",
                "message", message,
                "timestamp", LocalDateTime.now()
            );
            
            messagingTemplate.convertAndSendToUser(userId, "/queue/test", testMessage);
            
            logger.info("Sent test message to user {}: {}", userId, message);
            
        } catch (Exception e) {
            logger.error("Failed to send test message to user {}", userId, e);
        }
    }
}
