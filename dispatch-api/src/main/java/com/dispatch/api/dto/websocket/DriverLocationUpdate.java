package com.dispatch.api.dto.websocket;

import java.time.LocalDateTime;

public class DriverLocationUpdate {
    private String driverId;
    private double lat;
    private double lng;
    private double heading;
    private LocalDateTime timestamp;
    
    public DriverLocationUpdate() {}
    
    public DriverLocationUpdate(String driverId, double lat, double lng, double heading, LocalDateTime timestamp) {
        this.driverId = driverId;
        this.lat = lat;
        this.lng = lng;
        this.heading = heading;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    
    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }
    
    public double getHeading() { return heading; }
    public void setHeading(double heading) { this.heading = heading; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
