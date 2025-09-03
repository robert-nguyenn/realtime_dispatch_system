package com.dispatch.api.dto.mapper;

import com.dispatch.api.dto.response.RideResponse;
import com.dispatch.api.model.Ride;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RideMapper {
    
    @Mapping(target = "rideId", source = "id")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "pickupLocation", expression = "java(formatLocation(ride.getPickupLat(), ride.getPickupLng()))")
    @Mapping(target = "destinationLocation", expression = "java(formatLocation(ride.getDestinationLat(), ride.getDestinationLng()))")
    @Mapping(target = "estimatedFare", source = "estimatedFare")
    @Mapping(target = "actualFare", source = "fareAmount")
    @Mapping(target = "estimatedDuration", source = "estimatedDurationMinutes")
    @Mapping(target = "actualDuration", expression = "java(calculateActualDuration(ride))")
    RideResponse toResponse(Ride ride);
    
    default String formatLocation(Double lat, Double lng) {
        if (lat == null || lng == null) {
            return null;
        }
        return String.format("%.6f,%.6f", lat, lng);
    }
    
    default Integer calculateActualDuration(Ride ride) {
        if (ride.getStartedAt() == null || ride.getCompletedAt() == null) {
            return null;
        }
        return (int) java.time.Duration.between(ride.getStartedAt(), ride.getCompletedAt()).toMinutes();
    }
}
