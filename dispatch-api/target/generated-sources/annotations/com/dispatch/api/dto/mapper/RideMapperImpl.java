package com.dispatch.api.dto.mapper;

import com.dispatch.api.dto.response.RideResponse;
import com.dispatch.api.model.Ride;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-03T15:41:24-0400",
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

        if ( ride.getId() != null ) {
            rideResponse.setRideId( ride.getId().toString() );
        }
        rideResponse.setStatus( ride.getStatus() );
        rideResponse.setEstimatedFare( ride.getEstimatedFare() );
        rideResponse.setActualFare( ride.getFareAmount() );
        rideResponse.setEstimatedDuration( ride.getEstimatedDurationMinutes() );
        rideResponse.setAcceptedAt( ride.getAcceptedAt() );
        rideResponse.setCancelledAt( ride.getCancelledAt() );
        rideResponse.setCompletedAt( ride.getCompletedAt() );
        rideResponse.setCreatedAt( ride.getCreatedAt() );
        rideResponse.setDriverId( ride.getDriverId() );
        rideResponse.setRiderId( ride.getRiderId() );
        rideResponse.setStartedAt( ride.getStartedAt() );

        rideResponse.setPickupLocation( formatLocation(ride.getPickupLat(), ride.getPickupLng()) );
        rideResponse.setDestinationLocation( formatLocation(ride.getDestinationLat(), ride.getDestinationLng()) );
        rideResponse.setActualDuration( calculateActualDuration(ride) );

        return rideResponse;
    }
}
