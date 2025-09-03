package com.dispatch.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
public class Driver {
    
    @Id
    private String id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @Column
    private String phone;
    
    @Column(name = "license_plate")
    private String licensePlate;
    
    @Column(name = "current_lat", precision = 10, scale = 8)
    private BigDecimal currentLat;
    
    @Column(name = "current_lng", precision = 11, scale = 8)
    private BigDecimal currentLng;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status = DriverStatus.OFFLINE;
    
    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Constructors
    public Driver() {}
    
    public Driver(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    // Business methods
    public boolean isAvailable() {
        return status == DriverStatus.AVAILABLE;
    }
    
    public boolean canAcceptRide() {
        return isAvailable();
    }
    
    public void updateLocation(BigDecimal lat, BigDecimal lng) {
        this.currentLat = lat;
        this.currentLng = lng;
        this.lastLocationUpdate = LocalDateTime.now();
    }
    
    public void goOnline() {
        if (currentLat != null && currentLng != null) {
            this.status = DriverStatus.AVAILABLE;
        } else {
            throw new IllegalStateException("Cannot go online without location");
        }
    }
    
    public void goOffline() {
        this.status = DriverStatus.OFFLINE;
    }
    
    public void startRide() {
        if (!canAcceptRide()) {
            throw new IllegalStateException("Driver cannot start ride in current status: " + status);
        }
        this.status = DriverStatus.BUSY;
    }
    
    public void finishRide() {
        if (status != DriverStatus.BUSY) {
            throw new IllegalStateException("Driver is not busy");
        }
        this.status = DriverStatus.AVAILABLE;
    }
    
    public void setEnRoute() {
        this.status = DriverStatus.EN_ROUTE;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    
    public BigDecimal getCurrentLat() { return currentLat; }
    public void setCurrentLat(BigDecimal currentLat) { this.currentLat = currentLat; }
    
    public BigDecimal getCurrentLng() { return currentLng; }
    public void setCurrentLng(BigDecimal currentLng) { this.currentLng = currentLng; }
    
    public DriverStatus getStatus() { return status; }
    public void setStatus(DriverStatus status) { this.status = status; }
    
    public LocalDateTime getLastLocationUpdate() { return lastLocationUpdate; }
    public void setLastLocationUpdate(LocalDateTime lastLocationUpdate) { this.lastLocationUpdate = lastLocationUpdate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
