package com.dispatch.api.dto.websocket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SurgeUpdate {
    private double lat;
    private double lng;
    private double radiusKm;
    private BigDecimal surgeMultiplier;
    private LocalDateTime timestamp;
    
    public SurgeUpdate() {}
    
    public SurgeUpdate(double lat, double lng, double radiusKm, BigDecimal surgeMultiplier, LocalDateTime timestamp) {
        this.lat = lat;
        this.lng = lng;
        this.radiusKm = radiusKm;
        this.surgeMultiplier = surgeMultiplier;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    
    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }
    
    public double getRadiusKm() { return radiusKm; }
    public void setRadiusKm(double radiusKm) { this.radiusKm = radiusKm; }
    
    public BigDecimal getSurgeMultiplier() { return surgeMultiplier; }
    public void setSurgeMultiplier(BigDecimal surgeMultiplier) { this.surgeMultiplier = surgeMultiplier; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
