package com.dispatch.api.controller;

import com.dispatch.api.dto.response.RideResponse;
import com.dispatch.api.model.Ride;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RideMapper {
    
    RideMapper INSTANCE = Mappers.getMapper(RideMapper.class);
    
    RideResponse toResponse(Ride ride);
}
