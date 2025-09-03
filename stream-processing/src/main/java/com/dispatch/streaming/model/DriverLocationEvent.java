package com.dispatch.streaming.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class DriverLocationEvent {
    @JsonProperty("driverId")
    private String driverId;
    
    @JsonProperty("lat")
    private Double lat;
    
    @JsonProperty("lng")
    private Double lng;
    
    @JsonProperty("heading")
    private Integer heading;
    
    @JsonProperty("speedKmh")
    private Double speedKmh;
    
    @JsonProperty("accuracyMeters")
    private Double accuracyMeters;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonProperty("processingTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processingTime;
    
    // Constructors
    public DriverLocationEvent() {}
    
    // Getters and Setters
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    
    public Integer getHeading() { return heading; }
    public void setHeading(Integer heading) { this.heading = heading; }
    
    public Double getSpeedKmh() { return speedKmh; }
    public void setSpeedKmh(Double speedKmh) { this.speedKmh = speedKmh; }
    
    public Double getAccuracyMeters() { return accuracyMeters; }
    public void setAccuracyMeters(Double accuracyMeters) { this.accuracyMeters = accuracyMeters; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public LocalDateTime getProcessingTime() { return processingTime; }
    public void setProcessingTime(LocalDateTime processingTime) { this.processingTime = processingTime; }
}
