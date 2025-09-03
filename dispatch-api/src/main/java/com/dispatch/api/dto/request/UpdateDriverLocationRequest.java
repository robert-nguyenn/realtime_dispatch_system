package com.dispatch.api.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class UpdateDriverLocationRequest {
    
    @NotBlank(message = "Driver ID is required")
    private String driverId;
    
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal lat;
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal lng;
    
    @Min(value = 0, message = "Heading must be between 0 and 359")
    @Max(value = 359, message = "Heading must be between 0 and 359")
    private Integer heading;
    
    @DecimalMin(value = "0.0", message = "Speed cannot be negative")
    private BigDecimal speedKmh;
    
    @DecimalMin(value = "0.0", message = "Accuracy cannot be negative")
    private BigDecimal accuracyMeters;
    
    // Constructors
    public UpdateDriverLocationRequest() {}
    
    public UpdateDriverLocationRequest(String driverId, BigDecimal lat, BigDecimal lng) {
        this.driverId = driverId;
        this.lat = lat;
        this.lng = lng;
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
}
