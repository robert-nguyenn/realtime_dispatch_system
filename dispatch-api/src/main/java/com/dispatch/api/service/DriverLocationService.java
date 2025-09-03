package com.dispatch.api.service;

import com.dispatch.api.dto.events.DriverLocationEvent;
import com.dispatch.api.model.Driver;
import com.dispatch.api.model.DriverStatus;
import com.dispatch.api.repository.DriverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DriverLocationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DriverLocationService.class);
    
    private final DriverRepository driverRepository;
    private final GeoIndexService geoIndexService;
    private final EventPublishingService eventPublishingService;
    
    public DriverLocationService(DriverRepository driverRepository, 
                               GeoIndexService geoIndexService,
                               EventPublishingService eventPublishingService) {
        this.driverRepository = driverRepository;
        this.geoIndexService = geoIndexService;
        this.eventPublishingService = eventPublishingService;
    }
    
    public Driver updateDriverLocation(String driverId, BigDecimal lat, BigDecimal lng, 
                                     Integer heading, BigDecimal speedKmh, BigDecimal accuracyMeters) {
        
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));
        
        // Update location in database
        driver.updateLocation(lat, lng);
        driver = driverRepository.save(driver);
        
        // Update location in geo-index
        boolean geoIndexUpdated = geoIndexService.updateDriverLocation(
            driverId, lat, lng, driver.getStatus().name()
        );
        
        if (!geoIndexUpdated) {
            logger.warn("Failed to update driver {} location in geo-index", driverId);
        }
        
        // Publish location event to Kafka
        DriverLocationEvent locationEvent = new DriverLocationEvent(
            driverId, lat, lng, driver.getStatus().name()
        );
        locationEvent.setHeading(heading);
        locationEvent.setSpeedKmh(speedKmh);
        locationEvent.setAccuracyMeters(accuracyMeters);
        
        eventPublishingService.publishDriverLocationEvent(locationEvent);
        
        logger.debug("Updated location for driver {} to ({}, {})", driverId, lat, lng);
        
        return driver;
    }
    
    public Driver updateDriverStatus(String driverId, DriverStatus newStatus) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));
        
        DriverStatus oldStatus = driver.getStatus();
        driver.setStatus(newStatus);
        driver = driverRepository.save(driver);
        
        // Update status in geo-index if location is available
        if (driver.getCurrentLat() != null && driver.getCurrentLng() != null) {
            boolean geoIndexUpdated = geoIndexService.updateDriverLocation(
                driverId, 
                driver.getCurrentLat(), 
                driver.getCurrentLng(), 
                newStatus.name()
            );
            
            if (!geoIndexUpdated) {
                logger.warn("Failed to update driver {} status in geo-index", driverId);
            }
        }
        
        // If driver goes offline, remove from geo-index
        if (newStatus == DriverStatus.OFFLINE) {
            geoIndexService.removeDriver(driverId);
        }
        
        logger.info("Updated driver {} status from {} to {}", driverId, oldStatus, newStatus);
        
        return driver;
    }
    
    public Driver goOnline(String driverId, BigDecimal lat, BigDecimal lng) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));
        
        // Update location and go online
        driver.updateLocation(lat, lng);
        driver.goOnline();
        driver = driverRepository.save(driver);
        
        // Add to geo-index
        boolean geoIndexUpdated = geoIndexService.updateDriverLocation(
            driverId, lat, lng, DriverStatus.AVAILABLE.name()
        );
        
        if (!geoIndexUpdated) {
            logger.warn("Failed to add driver {} to geo-index", driverId);
        }
        
        // Publish location event
        DriverLocationEvent locationEvent = new DriverLocationEvent(
            driverId, lat, lng, DriverStatus.AVAILABLE.name()
        );
        eventPublishingService.publishDriverLocationEvent(locationEvent);
        
        logger.info("Driver {} went online at ({}, {})", driverId, lat, lng);
        
        return driver;
    }
    
    public Driver goOffline(String driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));
        
        driver.goOffline();
        driver = driverRepository.save(driver);
        
        // Remove from geo-index
        boolean removed = geoIndexService.removeDriver(driverId);
        if (!removed) {
            logger.warn("Failed to remove driver {} from geo-index", driverId);
        }
        
        logger.info("Driver {} went offline", driverId);
        
        return driver;
    }
    
    public List<Driver> getAvailableDriversInArea(BigDecimal lat, BigDecimal lng, double radiusKm) {
        // Calculate bounding box (simplified)
        double latDelta = radiusKm / 111.0; // Rough conversion: 1 degree lat ≈ 111 km
        double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat.doubleValue())));
        
        double minLat = lat.doubleValue() - latDelta;
        double maxLat = lat.doubleValue() + latDelta;
        double minLng = lng.doubleValue() - lngDelta;
        double maxLng = lng.doubleValue() + lngDelta;
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(5); // Consider stale after 5 minutes
        
        return driverRepository.findAvailableDriversInArea(
            DriverStatus.AVAILABLE, minLat, maxLat, minLng, maxLng, cutoffTime
        );
    }
    
    public void cleanupStaleDrivers() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(10); // Consider stale after 10 minutes
        List<Driver> staleDrivers = driverRepository.findStaleDrivers(cutoffTime);
        
        for (Driver driver : staleDrivers) {
            try {
                driver.goOffline();
                driverRepository.save(driver);
                
                // Remove from geo-index
                geoIndexService.removeDriver(driver.getId());
                
                logger.info("Marked stale driver {} as offline", driver.getId());
                
            } catch (Exception e) {
                logger.error("Error cleaning up stale driver {}", driver.getId(), e);
            }
        }
        
        if (!staleDrivers.isEmpty()) {
            logger.info("Cleaned up {} stale drivers", staleDrivers.size());
        }
    }
}
