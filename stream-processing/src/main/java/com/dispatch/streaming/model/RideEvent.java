package com.dispatch.streaming.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class RideEvent {
    @JsonProperty("rideId")
    private String rideId;
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("riderId")
    private String riderId;
    
    @JsonProperty("driverId")
    private String driverId;
    
    @JsonProperty("pickupLat")
    private Double pickupLat;
    
    @JsonProperty("pickupLng")
    private Double pickupLng;
    
    @JsonProperty("destinationLat")
    private Double destinationLat;
    
    @JsonProperty("destinationLng")
    private Double destinationLng;
    
    @JsonProperty("fareAmount")
    private Double fareAmount;
    
    @JsonProperty("estimatedDurationMinutes")
    private Integer estimatedDurationMinutes;
    
    @JsonProperty("actualDurationMinutes")
    private Integer actualDurationMinutes;
    
    @JsonProperty("surgeMultiplier")
    private Double surgeMultiplier = 1.0;
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonProperty("processingTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processingTime;
    
    // Constructors
    public RideEvent() {}
    
    // Getters and Setters
    public String getRideId() { return rideId; }
    public void setRideId(String rideId) { this.rideId = rideId; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getRiderId() { return riderId; }
    public void setRiderId(String riderId) { this.riderId = riderId; }
    
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    
    public Double getPickupLat() { return pickupLat; }
    public void setPickupLat(Double pickupLat) { this.pickupLat = pickupLat; }
    
    public Double getPickupLng() { return pickupLng; }
    public void setPickupLng(Double pickupLng) { this.pickupLng = pickupLng; }
    
    public Double getDestinationLat() { return destinationLat; }
    public void setDestinationLat(Double destinationLat) { this.destinationLat = destinationLat; }
    
    public Double getDestinationLng() { return destinationLng; }
    public void setDestinationLng(Double destinationLng) { this.destinationLng = destinationLng; }
    
    public Double getFareAmount() { return fareAmount; }
    public void setFareAmount(Double fareAmount) { this.fareAmount = fareAmount; }
    
    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }
    
    public Integer getActualDurationMinutes() { return actualDurationMinutes; }
    public void setActualDurationMinutes(Integer actualDurationMinutes) { this.actualDurationMinutes = actualDurationMinutes; }
    
    public Double getSurgeMultiplier() { return surgeMultiplier; }
    public void setSurgeMultiplier(Double surgeMultiplier) { this.surgeMultiplier = surgeMultiplier; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public LocalDateTime getProcessingTime() { return processingTime; }
    public void setProcessingTime(LocalDateTime processingTime) { this.processingTime = processingTime; }
}
