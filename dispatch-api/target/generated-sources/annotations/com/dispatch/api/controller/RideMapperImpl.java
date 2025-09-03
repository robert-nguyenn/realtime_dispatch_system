package com.dispatch.api.controller;

import com.dispatch.api.dto.response.RideResponse;
import com.dispatch.api.model.Ride;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-03T15:37:47-0400",
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

        rideResponse.setAcceptedAt( ride.getAcceptedAt() );
        rideResponse.setCancelledAt( ride.getCancelledAt() );
        rideResponse.setCompletedAt( ride.getCompletedAt() );
        rideResponse.setCreatedAt( ride.getCreatedAt() );
        rideResponse.setDestinationLat( ride.getDestinationLat() );
        rideResponse.setDestinationLng( ride.getDestinationLng() );
        rideResponse.setDriverId( ride.getDriverId() );
        rideResponse.setEstimatedDurationMinutes( ride.getEstimatedDurationMinutes() );
        rideResponse.setFareAmount( ride.getFareAmount() );
        rideResponse.setId( ride.getId() );
        rideResponse.setPickupLat( ride.getPickupLat() );
        rideResponse.setPickupLng( ride.getPickupLng() );
        rideResponse.setRiderId( ride.getRiderId() );
        rideResponse.setStartedAt( ride.getStartedAt() );
        rideResponse.setStatus( ride.getStatus() );

        return rideResponse;
    }
}
