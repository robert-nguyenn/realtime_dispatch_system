package com.dispatch.api.service;

import com.dispatch.api.grpc.GeoIndexProto.*;
import com.dispatch.api.grpc.GeoIndexServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class GeoIndexService {
    
    private static final Logger logger = LoggerFactory.getLogger(GeoIndexService.class);
    
    @Value("${app.geo-index.host}")
    private String geoIndexHost;
    
    @Value("${app.geo-index.port}")
    private int geoIndexPort;
    
    @Value("${app.geo-index.timeout}")
    private String timeout;
    
    private ManagedChannel channel;
    private GeoIndexServiceGrpc.GeoIndexServiceBlockingStub blockingStub;
    
    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(geoIndexHost, geoIndexPort)
                .usePlaintext()
                .build();
        blockingStub = GeoIndexServiceGrpc.newBlockingStub(channel);
        logger.info("GeoIndex gRPC client initialized for {}:{}", geoIndexHost, geoIndexPort);
    }
    
    @PreDestroy
    public void cleanup() {
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while shutting down gRPC channel", e);
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public List<DriverLocation> findNearestDrivers(BigDecimal lat, BigDecimal lng, int maxDrivers, double maxRadiusKm) {
        try {
            FindNearestDriversRequest request = FindNearestDriversRequest.newBuilder()
                    .setLat(lat.doubleValue())
                    .setLng(lng.doubleValue())
                    .setMaxDrivers(maxDrivers)
                    .setMaxRadiusKm(maxRadiusKm)
                    .build();
            
            FindNearestDriversResponse response = blockingStub.findNearestDrivers(request);
            logger.debug("Found {} drivers near ({}, {}) within {}km", 
                        response.getDriversCount(), lat, lng, maxRadiusKm);
            
            return response.getDriversList();
            
        } catch (StatusRuntimeException e) {
            logger.error("gRPC call failed when finding nearest drivers", e);
            throw new RuntimeException("Failed to find nearest drivers", e);
        }
    }
    
    public boolean updateDriverLocation(String driverId, BigDecimal lat, BigDecimal lng, String status) {
        try {
            DriverStatus grpcStatus = mapToGrpcStatus(status);
            
            UpdateDriverLocationRequest request = UpdateDriverLocationRequest.newBuilder()
                    .setDriverId(driverId)
                    .setLat(lat.doubleValue())
                    .setLng(lng.doubleValue())
                    .setStatus(grpcStatus)
                    .build();
            
            UpdateDriverLocationResponse response = blockingStub.updateDriverLocation(request);
            
            if (response.getSuccess()) {
                logger.debug("Updated location for driver {} to ({}, {}) with status {}", 
                           driverId, lat, lng, status);
            } else {
                logger.warn("Failed to update driver location: {}", response.getMessage());
            }
            
            return response.getSuccess();
            
        } catch (StatusRuntimeException e) {
            logger.error("gRPC call failed when updating driver location for driver: " + driverId, e);
            return false;
        }
    }
    
    public boolean removeDriver(String driverId) {
        try {
            RemoveDriverRequest request = RemoveDriverRequest.newBuilder()
                    .setDriverId(driverId)
                    .build();
            
            RemoveDriverResponse response = blockingStub.removeDriver(request);
            
            if (response.getSuccess()) {
                logger.debug("Removed driver {} from geo-index", driverId);
            } else {
                logger.warn("Failed to remove driver from geo-index: {}", response.getMessage());
            }
            
            return response.getSuccess();
            
        } catch (StatusRuntimeException e) {
            logger.error("gRPC call failed when removing driver: " + driverId, e);
            return false;
        }
    }
    
    public DriverLocation getDriverLocation(String driverId) {
        try {
            GetDriverLocationRequest request = GetDriverLocationRequest.newBuilder()
                    .setDriverId(driverId)
                    .build();
            
            GetDriverLocationResponse response = blockingStub.getDriverLocation(request);
            
            if (response.getFound()) {
                logger.debug("Found location for driver {}", driverId);
                return response.getDriver();
            } else {
                logger.debug("Driver {} not found in geo-index", driverId);
                return null;
            }
            
        } catch (StatusRuntimeException e) {
            logger.error("gRPC call failed when getting driver location for: " + driverId, e);
            return null;
        }
    }
    
    private DriverStatus mapToGrpcStatus(String status) {
        return switch (status.toUpperCase()) {
            case "AVAILABLE" -> DriverStatus.AVAILABLE;
            case "BUSY" -> DriverStatus.BUSY;
            case "EN_ROUTE" -> DriverStatus.EN_ROUTE;
            default -> DriverStatus.OFFLINE;
        };
    }
}
