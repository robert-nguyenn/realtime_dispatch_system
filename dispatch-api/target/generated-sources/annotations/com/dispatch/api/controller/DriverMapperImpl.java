package com.dispatch.api.controller;

import com.dispatch.api.dto.response.DriverResponse;
import com.dispatch.api.model.Driver;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-03T15:33:11-0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.50.v20250729-0351, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class DriverMapperImpl implements DriverMapper {

    @Override
    public DriverResponse toResponse(Driver driver) {
        if ( driver == null ) {
            return null;
        }

        DriverResponse driverResponse = new DriverResponse();

        driverResponse.setCreatedAt( driver.getCreatedAt() );
        driverResponse.setCurrentLat( driver.getCurrentLat() );
        driverResponse.setCurrentLng( driver.getCurrentLng() );
        driverResponse.setId( driver.getId() );
        driverResponse.setLastLocationUpdate( driver.getLastLocationUpdate() );
        driverResponse.setLicensePlate( driver.getLicensePlate() );
        driverResponse.setName( driver.getName() );
        driverResponse.setPhone( driver.getPhone() );
        driverResponse.setStatus( driver.getStatus() );

        return driverResponse;
    }
}
