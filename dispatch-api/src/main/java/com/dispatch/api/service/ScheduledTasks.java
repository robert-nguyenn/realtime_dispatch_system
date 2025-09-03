package com.dispatch.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTasks {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    
    private final DriverLocationService driverLocationService;
    
    public ScheduledTasks(DriverLocationService driverLocationService) {
        this.driverLocationService = driverLocationService;
    }
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupStaleDrivers() {
        try {
            driverLocationService.cleanupStaleDrivers();
        } catch (Exception e) {
            logger.error("Error during scheduled cleanup of stale drivers", e);
        }
    }
}
