package com.dispatch.api.service;

import com.dispatch.api.dto.events.RideEvent;
import com.dispatch.api.grpc.GeoIndexProto.DriverLocation;
import com.dispatch.api.model.Driver;
import com.dispatch.api.model.Ride;
import com.dispatch.api.repository.DriverRepository;
import com.dispatch.api.repository.RideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DispatchService {
    
    private static final Logger logger = LoggerFactory.getLogger(DispatchService.class);
    
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    private final GeoIndexService geoIndexService;
    private final EventPublishingService eventPublishingService;
    private final FeatureFlagService featureFlagService;
    
    @Value("${app.matching.max-search-radius-km}")
    private double maxSearchRadiusKm;
    
    @Value("${app.matching.max-drivers-to-consider}")
    private int maxDriversToConsider;
    
    @Value("${app.matching.assignment-timeout-seconds}")
    private int assignmentTimeoutSeconds;
    
    public DispatchService(RideRepository rideRepository, 
                          DriverRepository driverRepository,
                          GeoIndexService geoIndexService, 
                          EventPublishingService eventPublishingService,
                          FeatureFlagService featureFlagService) {
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
        this.geoIndexService = geoIndexService;
        this.eventPublishingService = eventPublishingService;
        this.featureFlagService = featureFlagService;
    }
    
    public Ride createRide(String riderId, BigDecimal pickupLat, BigDecimal pickupLng, 
                          BigDecimal destinationLat, BigDecimal destinationLng) {
        
        // Check if rider has any active rides
        List<com.dispatch.api.model.RideStatus> activeStatuses = List.of(
            com.dispatch.api.model.RideStatus.REQUESTED,
            com.dispatch.api.model.RideStatus.ACCEPTED,
            com.dispatch.api.model.RideStatus.IN_PROGRESS
        );
        
        Optional<Ride> existingRide = rideRepository.findByRiderIdAndStatusIn(riderId, activeStatuses);
        if (existingRide.isPresent()) {
            throw new IllegalStateException("Rider already has an active ride");
        }
        
        // Create new ride
        Ride ride = new Ride(riderId, pickupLat, pickupLng);
        ride.setDestinationLat(destinationLat);
        ride.setDestinationLng(destinationLng);
        
        ride = rideRepository.save(ride);
        
        // Publish ride requested event
        RideEvent rideEvent = RideEvent.requested(ride.getId(), riderId, pickupLat, pickupLng);
        rideEvent.setDestinationLat(destinationLat);
        rideEvent.setDestinationLng(destinationLng);
        eventPublishingService.publishRideEvent(rideEvent);
        
        // Attempt to match with driver immediately
        attemptDriverMatching(ride);
        
        logger.info("Created ride {} for rider {} at ({}, {})", 
                   ride.getId(), riderId, pickupLat, pickupLng);
        
        return ride;
    }
    
    public void attemptDriverMatching(Ride ride) {
        if (!ride.canBeAccepted()) {
            logger.debug("Ride {} cannot be matched in current status: {}", ride.getId(), ride.getStatus());
            return;
        }
        
        try {
            // Check feature flag for matching strategy
            boolean useAdvancedMatching = featureFlagService.isFeatureEnabled("advanced_matching", "default");
            
            List<DriverLocation> nearbyDrivers = geoIndexService.findNearestDrivers(
                ride.getPickupLat(), 
                ride.getPickupLng(), 
                maxDriversToConsider, 
                maxSearchRadiusKm
            );
            
            if (nearbyDrivers.isEmpty()) {
                logger.info("No available drivers found for ride {} within {}km", 
                           ride.getId(), maxSearchRadiusKm);
                return;
            }
            
            // Select best driver based on strategy
            DriverLocation selectedDriver = useAdvancedMatching ? 
                selectDriverAdvanced(nearbyDrivers, ride) : 
                selectDriverSimple(nearbyDrivers);
            
            if (selectedDriver != null) {
                assignRideToDriver(ride, selectedDriver.getDriverId());
            }
            
        } catch (Exception e) {
            logger.error("Error during driver matching for ride {}", ride.getId(), e);
        }
    }
    
    private DriverLocation selectDriverSimple(List<DriverLocation> drivers) {
        // Simple strategy: select closest driver
        return drivers.stream()
                .filter(d -> d.getStatus() == com.dispatch.api.grpc.GeoIndexProto.DriverStatus.AVAILABLE)
                .findFirst()
                .orElse(null);
    }
    
    private DriverLocation selectDriverAdvanced(List<DriverLocation> drivers, Ride ride) {
        // Advanced strategy: consider distance, driver rating, surge area, etc.
        // For now, implement simple distance-based selection
        return drivers.stream()
                .filter(d -> d.getStatus() == com.dispatch.api.grpc.GeoIndexProto.DriverStatus.AVAILABLE)
                .min((d1, d2) -> Double.compare(d1.getDistanceKm(), d2.getDistanceKm()))
                .orElse(null);
    }
    
    private void assignRideToDriver(Ride ride, String driverId) {
        try {
            // Verify driver exists and is available
            Optional<Driver> driverOpt = driverRepository.findById(driverId);
            if (driverOpt.isEmpty()) {
                logger.warn("Driver {} not found in database", driverId);
                return;
            }
            
            Driver driver = driverOpt.get();
            if (!driver.canAcceptRide()) {
                logger.warn("Driver {} cannot accept ride in current status: {}", driverId, driver.getStatus());
                return;
            }
            
            // Accept the ride
            ride.accept(driverId);
            driver.startRide();
            
            // Save changes
            rideRepository.save(ride);
            driverRepository.save(driver);
            
            // Update driver status in geo-index
            geoIndexService.updateDriverLocation(
                driverId, 
                driver.getCurrentLat(), 
                driver.getCurrentLng(), 
                "BUSY"
            );
            
            // Publish events
            RideEvent acceptedEvent = RideEvent.accepted(ride.getId(), ride.getRiderId(), driverId);
            eventPublishingService.publishRideEvent(acceptedEvent);
            eventPublishingService.publishRideAssignmentEvent(
                ride.getId().toString(), 
                driverId, 
                "ASSIGNED"
            );
            
            logger.info("Assigned ride {} to driver {}", ride.getId(), driverId);
            
        } catch (Exception e) {
            logger.error("Error assigning ride {} to driver {}", ride.getId(), driverId, e);
        }
    }
    
    public Ride startRide(UUID rideId, String driverId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));
        
        if (!driverId.equals(ride.getDriverId())) {
            throw new IllegalArgumentException("Driver not assigned to this ride");
        }
        
        if (!ride.canBeStarted()) {
            throw new IllegalStateException("Ride cannot be started in current status: " + ride.getStatus());
        }
        
        ride.start();
        ride = rideRepository.save(ride);
        
        // Publish started event
        RideEvent startedEvent = RideEvent.started(ride.getId(), ride.getRiderId(), driverId);
        eventPublishingService.publishRideEvent(startedEvent);
        
        logger.info("Started ride {} with driver {}", rideId, driverId);
        
        return ride;
    }
    
    public Ride completeRide(UUID rideId, String driverId, BigDecimal fareAmount) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));
        
        if (!driverId.equals(ride.getDriverId())) {
            throw new IllegalArgumentException("Driver not assigned to this ride");
        }
        
        if (!ride.canBeCompleted()) {
            throw new IllegalStateException("Ride cannot be completed in current status: " + ride.getStatus());
        }
        
        ride.setFareAmount(fareAmount);
        ride.complete();
        ride = rideRepository.save(ride);
        
        // Update driver status
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
        driver.finishRide();
        driverRepository.save(driver);
        
        // Update driver status in geo-index
        geoIndexService.updateDriverLocation(
            driverId, 
            driver.getCurrentLat(), 
            driver.getCurrentLng(), 
            "AVAILABLE"
        );
        
        // Calculate duration
        Integer durationMinutes = null;
        if (ride.getStartedAt() != null) {
            durationMinutes = (int) java.time.Duration.between(ride.getStartedAt(), ride.getCompletedAt()).toMinutes();
        }
        
        // Publish completed event
        RideEvent completedEvent = RideEvent.completed(
            ride.getId(), 
            ride.getRiderId(), 
            driverId, 
            fareAmount, 
            durationMinutes
        );
        eventPublishingService.publishRideEvent(completedEvent);
        
        logger.info("Completed ride {} with driver {} for fare ${}", rideId, driverId, fareAmount);
        
        return ride;
    }
    
    public Ride cancelRide(UUID rideId, String initiatedBy) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));
        
        if (!ride.canBeCancelled()) {
            throw new IllegalStateException("Ride cannot be cancelled in current status: " + ride.getStatus());
        }
        
        String driverId = ride.getDriverId();
        ride.cancel();
        ride = rideRepository.save(ride);
        
        // If driver was assigned, update their status
        if (driverId != null) {
            Optional<Driver> driverOpt = driverRepository.findById(driverId);
            if (driverOpt.isPresent()) {
                Driver driver = driverOpt.get();
                driver.finishRide();
                driverRepository.save(driver);
                
                // Update driver status in geo-index
                geoIndexService.updateDriverLocation(
                    driverId, 
                    driver.getCurrentLat(), 
                    driver.getCurrentLng(), 
                    "AVAILABLE"
                );
            }
        }
        
        // Publish cancelled event
        RideEvent cancelledEvent = RideEvent.cancelled(ride.getId(), ride.getRiderId(), driverId);
        eventPublishingService.publishRideEvent(cancelledEvent);
        
        logger.info("Cancelled ride {} initiated by {}", rideId, initiatedBy);
        
        return ride;
    }
    
    /**
     * Get count of available drivers in specified area
     */
    public int getNearbyDriversCount(double lat, double lng, int radiusMeters) {
        try {
            List<DriverLocation> nearbyDrivers = geoIndexService.findNearbyDrivers(lat, lng, radiusMeters, 100);
            return nearbyDrivers.size();
        } catch (Exception e) {
            logger.error("Error getting nearby drivers count for location ({}, {}) with radius {}", 
                lat, lng, radiusMeters, e);
            return 0;
        }
    }
}
