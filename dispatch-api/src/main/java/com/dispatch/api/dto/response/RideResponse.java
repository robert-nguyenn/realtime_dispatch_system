package com.dispatch.api.dto.response;

import com.dispatch.api.model.RideStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class RideResponse {
    
    private String rideId;  // Changed from UUID id to String rideId
    private String riderId;
    private String driverId;
    private String pickupLocation;
    private String destinationLocation;
    private RideStatus status;
    private BigDecimal estimatedFare;
    private BigDecimal actualFare;
    private Integer estimatedDuration;
    private Integer actualDuration;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    
    // Constructors
    public RideResponse() {}
    
    // Getters and Setters
    public String getRideId() { return rideId; }
    public void setRideId(String rideId) { this.rideId = rideId; }
    
    public String getRiderId() { return riderId; }
    public void setRiderId(String riderId) { this.riderId = riderId; }
    
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    
    public String getDestinationLocation() { return destinationLocation; }
    public void setDestinationLocation(String destinationLocation) { this.destinationLocation = destinationLocation; }
    
    public RideStatus getStatus() { return status; }
    public void setStatus(RideStatus status) { this.status = status; }
    
    public BigDecimal getEstimatedFare() { return estimatedFare; }
    public void setEstimatedFare(BigDecimal estimatedFare) { this.estimatedFare = estimatedFare; }
    
    public BigDecimal getActualFare() { return actualFare; }
    public void setActualFare(BigDecimal actualFare) { this.actualFare = actualFare; }
    
    public Integer getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }
    
    public Integer getActualDuration() { return actualDuration; }
    public void setActualDuration(Integer actualDuration) { this.actualDuration = actualDuration; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
}
