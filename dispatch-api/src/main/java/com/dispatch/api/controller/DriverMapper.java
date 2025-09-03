package com.dispatch.api.controller;

import com.dispatch.api.dto.response.DriverResponse;
import com.dispatch.api.model.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DriverMapper {
    
    DriverMapper INSTANCE = Mappers.getMapper(DriverMapper.class);
    
    DriverResponse toResponse(Driver driver);
}
