package com.dispatch.api.repository;

import com.dispatch.api.model.Driver;
import com.dispatch.api.model.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {
    
    // Find drivers by status
    List<Driver> findByStatus(DriverStatus status);
    
    // Find available drivers
    List<Driver> findByStatusOrderByLastLocationUpdateDesc(DriverStatus status);
    
    // Find drivers in area (simple bounding box)
    @Query("SELECT d FROM Driver d WHERE d.status = :status " +
           "AND d.currentLat BETWEEN :minLat AND :maxLat " +
           "AND d.currentLng BETWEEN :minLng AND :maxLng " +
           "AND d.lastLocationUpdate > :cutoffTime " +
           "ORDER BY d.lastLocationUpdate DESC")
    List<Driver> findAvailableDriversInArea(@Param("status") DriverStatus status,
                                           @Param("minLat") Double minLat, @Param("maxLat") Double maxLat,
                                           @Param("minLng") Double minLng, @Param("maxLng") Double maxLng,
                                           @Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Find drivers with recent location updates
    @Query("SELECT d FROM Driver d WHERE d.lastLocationUpdate > :cutoffTime ORDER BY d.lastLocationUpdate DESC")
    List<Driver> findDriversWithRecentUpdates(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Find stale drivers (haven't updated location recently)
    @Query("SELECT d FROM Driver d WHERE d.status != 'OFFLINE' AND (d.lastLocationUpdate IS NULL OR d.lastLocationUpdate < :cutoffTime)")
    List<Driver> findStaleDrivers(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Count drivers by status
    long countByStatus(DriverStatus status);
    
    // Count active drivers in time period
    @Query("SELECT COUNT(DISTINCT d.id) FROM Driver d WHERE d.lastLocationUpdate BETWEEN :startTime AND :endTime")
    long countActiveDriversInPeriod(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
