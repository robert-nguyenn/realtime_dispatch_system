package com.dispatch.api.dto.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DriverLocationEvent {
    
    private String driverId;
    private BigDecimal lat;
    private BigDecimal lng;
    private Integer heading;
    private BigDecimal speedKmh;
    private BigDecimal accuracyMeters;
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processingTime;
    
    // Constructors
    public DriverLocationEvent() {
        this.timestamp = LocalDateTime.now();
        this.processingTime = LocalDateTime.now();
    }
    
    public DriverLocationEvent(String driverId, BigDecimal lat, BigDecimal lng, String status) {
        this();
        this.driverId = driverId;
        this.lat = lat;
        this.lng = lng;
        this.status = status;
    }
    
    // Getters and Setters
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    
    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }
    
    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }
    
    public Integer getHeading() { return heading; }
    public void setHeading(Integer heading) { this.heading = heading; }
    
    public BigDecimal getSpeedKmh() { return speedKmh; }
    public void setSpeedKmh(BigDecimal speedKmh) { this.speedKmh = speedKmh; }
    
    public BigDecimal getAccuracyMeters() { return accuracyMeters; }
    public void setAccuracyMeters(BigDecimal accuracyMeters) { this.accuracyMeters = accuracyMeters; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public LocalDateTime getProcessingTime() { return processingTime; }
    public void setProcessingTime(LocalDateTime processingTime) { this.processingTime = processingTime; }
}
