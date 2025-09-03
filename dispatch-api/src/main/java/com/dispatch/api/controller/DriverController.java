package com.dispatch.api.controller;

import com.dispatch.api.dto.request.UpdateDriverLocationRequest;
import com.dispatch.api.dto.response.DriverResponse;
import com.dispatch.api.model.Driver;
import com.dispatch.api.model.DriverStatus;
import com.dispatch.api.repository.DriverRepository;
import com.dispatch.api.service.DriverLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/drivers")
@Tag(name = "Drivers", description = "Driver management operations")
public class DriverController {
    
    private static final Logger logger = LoggerFactory.getLogger(DriverController.class);
    
    private final DriverRepository driverRepository;
    private final DriverLocationService driverLocationService;
    private final DriverMapper driverMapper;
    
    public DriverController(DriverRepository driverRepository, 
                           DriverLocationService driverLocationService,
                           DriverMapper driverMapper) {
        this.driverRepository = driverRepository;
        this.driverLocationService = driverLocationService;
        this.driverMapper = driverMapper;
    }
    
    @GetMapping("/{driverId}")
    @Operation(summary = "Get driver by ID")
    public ResponseEntity<DriverResponse> getDriver(
            @Parameter(description = "Driver ID") @PathVariable String driverId) {
        
        return driverRepository.findById(driverId)
                .map(driverMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Get all drivers")
    public ResponseEntity<List<DriverResponse>> getAllDrivers(
            @RequestParam(required = false) DriverStatus status) {
        
        List<Driver> drivers;
        if (status != null) {
            drivers = driverRepository.findByStatus(status);
        } else {
            drivers = driverRepository.findAll();
        }
        
        List<DriverResponse> response = drivers.stream()
                .map(driverMapper::toResponse)
                .toList();
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{driverId}/location")
    @Operation(summary = "Update driver location")
    public ResponseEntity<DriverResponse> updateDriverLocation(
            @Parameter(description = "Driver ID") @PathVariable String driverId,
            @Valid @RequestBody UpdateDriverLocationRequest request) {
        
        try {
            // Validate that the driver ID in path matches request
            if (!driverId.equals(request.getDriverId())) {
                return ResponseEntity.badRequest().build();
            }
            
            Driver driver = driverLocationService.updateDriverLocation(
                driverId,
                request.getLat(),
                request.getLng(),
                request.getHeading(),
                request.getSpeedKmh(),
                request.getAccuracyMeters()
            );
            
            DriverResponse response = driverMapper.toResponse(driver);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating location for driver {}", driverId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/{driverId}/status")
    @Operation(summary = "Update driver status")
    public ResponseEntity<DriverResponse> updateDriverStatus(
            @Parameter(description = "Driver ID") @PathVariable String driverId,
            @RequestParam DriverStatus status) {
        
        try {
            Driver driver = driverLocationService.updateDriverStatus(driverId, status);
            DriverResponse response = driverMapper.toResponse(driver);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating status for driver {}", driverId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/{driverId}/online")
    @Operation(summary = "Set driver online")
    public ResponseEntity<DriverResponse> goOnline(
            @Parameter(description = "Driver ID") @PathVariable String driverId,
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng) {
        
        try {
            Driver driver = driverLocationService.goOnline(driverId, lat, lng);
            DriverResponse response = driverMapper.toResponse(driver);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error setting driver {} online", driverId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/{driverId}/offline")
    @Operation(summary = "Set driver offline")
    public ResponseEntity<DriverResponse> goOffline(
            @Parameter(description = "Driver ID") @PathVariable String driverId) {
        
        try {
            Driver driver = driverLocationService.goOffline(driverId);
            DriverResponse response = driverMapper.toResponse(driver);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error setting driver {} offline", driverId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/nearby")
    @Operation(summary = "Find nearby available drivers")
    public ResponseEntity<List<DriverResponse>> getNearbyDrivers(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(defaultValue = "10.0") double radiusKm) {
        
        try {
            List<Driver> nearbyDrivers = driverLocationService.getAvailableDriversInArea(lat, lng, radiusKm);
            List<DriverResponse> response = nearbyDrivers.stream()
                    .map(driverMapper::toResponse)
                    .toList();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error finding nearby drivers at ({}, {})", lat, lng, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
