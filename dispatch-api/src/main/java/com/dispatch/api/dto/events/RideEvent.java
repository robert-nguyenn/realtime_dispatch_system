package com.dispatch.api.dto.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class RideEvent {
    
    private UUID rideId;
    private String eventType;
    private String riderId;
    private String driverId;
    private BigDecimal pickupLat;
    private BigDecimal pickupLng;
    private BigDecimal destinationLat;
    private BigDecimal destinationLng;
    private BigDecimal fareAmount;
    private Integer estimatedDurationMinutes;
    private Integer actualDurationMinutes;
    private BigDecimal surgeMultiplier = BigDecimal.valueOf(1.0);
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processingTime;
    
    // Constructors
    public RideEvent() {
        this.timestamp = LocalDateTime.now();
        this.processingTime = LocalDateTime.now();
    }
    
    public RideEvent(UUID rideId, String eventType, String riderId) {
        this();
        this.rideId = rideId;
        this.eventType = eventType;
        this.riderId = riderId;
    }
    
    // Static factory methods
    public static RideEvent requested(UUID rideId, String riderId, BigDecimal pickupLat, BigDecimal pickupLng) {
        RideEvent event = new RideEvent(rideId, "REQUESTED", riderId);
        event.setPickupLat(pickupLat);
        event.setPickupLng(pickupLng);
        return event;
    }
    
    public static RideEvent accepted(UUID rideId, String riderId, String driverId) {
        RideEvent event = new RideEvent(rideId, "ACCEPTED", riderId);
        event.setDriverId(driverId);
        return event;
    }
    
    public static RideEvent started(UUID rideId, String riderId, String driverId) {
        RideEvent event = new RideEvent(rideId, "STARTED", riderId);
        event.setDriverId(driverId);
        return event;
    }
    
    public static RideEvent completed(UUID rideId, String riderId, String driverId, BigDecimal fareAmount, Integer durationMinutes) {
        RideEvent event = new RideEvent(rideId, "COMPLETED", riderId);
        event.setDriverId(driverId);
        event.setFareAmount(fareAmount);
        event.setActualDurationMinutes(durationMinutes);
        return event;
    }
    
    public static RideEvent cancelled(UUID rideId, String riderId, String driverId) {
        RideEvent event = new RideEvent(rideId, "CANCELLED", riderId);
        event.setDriverId(driverId);
        return event;
    }
    
    // Getters and Setters
    public UUID getRideId() { return rideId; }
    public void setRideId(UUID rideId) { this.rideId = rideId; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getRiderId() { return riderId; }
    public void setRiderId(String riderId) { this.riderId = riderId; }
    
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    
    public BigDecimal getPickupLat() { return pickupLat; }
    public void setPickupLat(BigDecimal pickupLat) { this.pickupLat = pickupLat; }
    
    public BigDecimal getPickupLng() { return pickupLng; }
    public void setPickupLng(BigDecimal pickupLng) { this.pickupLng = pickupLng; }
    
    public BigDecimal getDestinationLat() { return destinationLat; }
    public void setDestinationLat(BigDecimal destinationLat) { this.destinationLat = destinationLat; }
    
    public BigDecimal getDestinationLng() { return destinationLng; }
    public void setDestinationLng(BigDecimal destinationLng) { this.destinationLng = destinationLng; }
    
    public BigDecimal getFareAmount() { return fareAmount; }
    public void setFareAmount(BigDecimal fareAmount) { this.fareAmount = fareAmount; }
    
    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }
    
    public Integer getActualDurationMinutes() { return actualDurationMinutes; }
    public void setActualDurationMinutes(Integer actualDurationMinutes) { this.actualDurationMinutes = actualDurationMinutes; }
    
    public BigDecimal getSurgeMultiplier() { return surgeMultiplier; }
    public void setSurgeMultiplier(BigDecimal surgeMultiplier) { this.surgeMultiplier = surgeMultiplier; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public LocalDateTime getProcessingTime() { return processingTime; }
    public void setProcessingTime(LocalDateTime processingTime) { this.processingTime = processingTime; }
}
