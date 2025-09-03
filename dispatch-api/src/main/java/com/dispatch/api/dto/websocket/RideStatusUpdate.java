package com.dispatch.api.dto.websocket;

import java.util.Map;

public class RideStatusUpdate {
    private String rideId;
    private String status;
    private String message;
    private Map<String, Object> details;
    
    public RideStatusUpdate() {}
    
    public RideStatusUpdate(String rideId, String status, String message, Map<String, Object> details) {
        this.rideId = rideId;
        this.status = status;
        this.message = message;
        this.details = details;
    }
    
    // Getters and setters
    public String getRideId() { return rideId; }
    public void setRideId(String rideId) { this.rideId = rideId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
}
