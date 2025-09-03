package com.dispatch.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rides")
public class Ride {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    @Column(name = "rider_id", nullable = false)
    private String riderId;
    
    @Column(name = "driver_id")
    private String driverId;
    
    @NotNull
    @Column(name = "pickup_lat", nullable = false, precision = 10, scale = 8)
    private BigDecimal pickupLat;
    
    @NotNull
    @Column(name = "pickup_lng", nullable = false, precision = 11, scale = 8)
    private BigDecimal pickupLng;
    
    @Column(name = "destination_lat", precision = 10, scale = 8)
    private BigDecimal destinationLat;
    
    @Column(name = "destination_lng", precision = 11, scale = 8)
    private BigDecimal destinationLng;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status = RideStatus.REQUESTED;
    
    @Column(name = "fare_amount", precision = 10, scale = 2)
    private BigDecimal fareAmount;
    
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Constructors
    public Ride() {}
    
    public Ride(String riderId, BigDecimal pickupLat, BigDecimal pickupLng) {
        this.riderId = riderId;
        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
    }
    
    // Business methods
    public boolean canBeAccepted() {
        return status == RideStatus.REQUESTED;
    }
    
    public boolean canBeStarted() {
        return status == RideStatus.ACCEPTED;
    }
    
    public boolean canBeCompleted() {
        return status == RideStatus.IN_PROGRESS;
    }
    
    public boolean canBeCancelled() {
        return status == RideStatus.REQUESTED || status == RideStatus.ACCEPTED;
    }
    
    public void accept(String driverId) {
        if (!canBeAccepted()) {
            throw new IllegalStateException("Ride cannot be accepted in current status: " + status);
        }
        this.driverId = driverId;
        this.status = RideStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }
    
    public void start() {
        if (!canBeStarted()) {
            throw new IllegalStateException("Ride cannot be started in current status: " + status);
        }
        this.status = RideStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }
    
    public void complete() {
        if (!canBeCompleted()) {
            throw new IllegalStateException("Ride cannot be completed in current status: " + status);
        }
        this.status = RideStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Ride cannot be cancelled in current status: " + status);
        }
        this.status = RideStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
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
    
    public RideStatus getStatus() { return status; }
    public void setStatus(RideStatus status) { this.status = status; }
    
    public BigDecimal getFareAmount() { return fareAmount; }
    public void setFareAmount(BigDecimal fareAmount) { this.fareAmount = fareAmount; }
    
    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }
    
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
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
