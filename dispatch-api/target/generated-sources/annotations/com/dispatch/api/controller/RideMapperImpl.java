package com.dispatch.api.controller;

import com.dispatch.api.dto.response.RideResponse;
import com.dispatch.api.model.Ride;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-03T15:18:35-0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.50.v20250729-0351, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class RideMapperImpl implements RideMapper {

    @Override
    public RideResponse toResponse(Ride ride) {
        if ( ride == null ) {
            return null;
        }

        RideResponse rideResponse = new RideResponse();

        rideResponse.setId( ride.getId() );
        rideResponse.setRiderId( ride.getRiderId() );
        rideResponse.setDriverId( ride.getDriverId() );
        rideResponse.setPickupLat( ride.getPickupLat() );
        rideResponse.setPickupLng( ride.getPickupLng() );
        rideResponse.setDestinationLat( ride.getDestinationLat() );
        rideResponse.setDestinationLng( ride.getDestinationLng() );
        rideResponse.setStatus( ride.getStatus() );
        rideResponse.setFareAmount( ride.getFareAmount() );
        rideResponse.setEstimatedDurationMinutes( ride.getEstimatedDurationMinutes() );
        rideResponse.setCreatedAt( ride.getCreatedAt() );
        rideResponse.setAcceptedAt( ride.getAcceptedAt() );
        rideResponse.setStartedAt( ride.getStartedAt() );
        rideResponse.setCompletedAt( ride.getCompletedAt() );
        rideResponse.setCancelledAt( ride.getCancelledAt() );

        return rideResponse;
    }
}
