package com.dispatch.api.controller;

import com.dispatch.api.dto.request.CreateRideRequest;
import com.dispatch.api.dto.response.RideResponse;
import com.dispatch.api.dto.mapper.RideMapper;
import com.dispatch.api.model.Ride;
import com.dispatch.api.model.RideStatus;
import com.dispatch.api.repository.RideRepository;
import com.dispatch.api.service.DispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rides")
@Tag(name = "Rides", description = "Ride management operations")
public class RideController {
    
    private static final Logger logger = LoggerFactory.getLogger(RideController.class);
    
    private final DispatchService dispatchService;
    private final RideRepository rideRepository;
    private final RideMapper rideMapper;
    
    public RideController(DispatchService dispatchService, 
                         RideRepository rideRepository,
                         RideMapper rideMapper) {
        this.dispatchService = dispatchService;
        this.rideRepository = rideRepository;
        this.rideMapper = rideMapper;
    }
    
    @PostMapping
    @Operation(summary = "Create a new ride request")
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody CreateRideRequest request) {
        try {
            Ride ride = dispatchService.createRide(
                request.getRiderId(),
                request.getPickupLat(),
                request.getPickupLng(),
                request.getDestinationLat(),
                request.getDestinationLng()
            );
            
            RideResponse response = rideMapper.toResponse(ride);
            logger.info("Created ride {} for rider {}", ride.getId(), request.getRiderId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating ride for rider {}", request.getRiderId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{rideId}")
    @Operation(summary = "Get ride by ID")
    public ResponseEntity<RideResponse> getRide(
            @Parameter(description = "Ride ID") @PathVariable UUID rideId) {
        
        return rideRepository.findById(rideId)
                .map(rideMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Get rides with optional filtering")
    public ResponseEntity<Page<RideResponse>> getRides(
            @RequestParam(required = false) String riderId,
            @RequestParam(required = false) String driverId,
            @RequestParam(required = false) RideStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? 
            Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Ride> rides = rideRepository.findAll(pageable);
        Page<RideResponse> response = rides.map(rideMapper::toResponse);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/rider/{riderId}")
    @Operation(summary = "Get rides for a specific rider")
    public ResponseEntity<List<RideResponse>> getRidesByRider(
            @Parameter(description = "Rider ID") @PathVariable String riderId) {
        
        List<Ride> rides = rideRepository.findByRiderIdOrderByCreatedAtDesc(riderId);
        List<RideResponse> response = rides.stream()
                .map(rideMapper::toResponse)
                .toList();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get rides for a specific driver")
    public ResponseEntity<List<RideResponse>> getRidesByDriver(
            @Parameter(description = "Driver ID") @PathVariable String driverId) {
        
        List<Ride> rides = rideRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
        List<RideResponse> response = rides.stream()
                .map(rideMapper::toResponse)
                .toList();
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{rideId}/start")
    @Operation(summary = "Start a ride")
    public ResponseEntity<RideResponse> startRide(
            @Parameter(description = "Ride ID") @PathVariable UUID rideId,
            @RequestParam String driverId) {
        
        try {
            Ride ride = dispatchService.startRide(rideId, driverId);
            RideResponse response = rideMapper.toResponse(ride);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error starting ride {} for driver {}", rideId, driverId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/{rideId}/complete")
    @Operation(summary = "Complete a ride")
    public ResponseEntity<RideResponse> completeRide(
            @Parameter(description = "Ride ID") @PathVariable UUID rideId,
            @RequestParam String driverId,
            @RequestParam BigDecimal fareAmount) {
        
        try {
            Ride ride = dispatchService.completeRide(rideId, driverId, fareAmount);
            RideResponse response = rideMapper.toResponse(ride);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error completing ride {} for driver {}", rideId, driverId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/{rideId}/cancel")
    @Operation(summary = "Cancel a ride")
    public ResponseEntity<RideResponse> cancelRide(
            @Parameter(description = "Ride ID") @PathVariable UUID rideId,
            @RequestParam String initiatedBy) {
        
        try {
            Ride ride = dispatchService.cancelRide(rideId, initiatedBy);
            RideResponse response = rideMapper.toResponse(ride);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error cancelling ride {} initiated by {}", rideId, initiatedBy, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
