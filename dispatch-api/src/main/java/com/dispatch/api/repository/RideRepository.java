package com.dispatch.api.repository;

import com.dispatch.api.model.Ride;
import com.dispatch.api.model.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RideRepository extends JpaRepository<Ride, UUID> {
    
    // Find rides by rider
    List<Ride> findByRiderIdOrderByCreatedAtDesc(String riderId);
    
    // Find rides by driver
    List<Ride> findByDriverIdOrderByCreatedAtDesc(String driverId);
    
    // Find rides by status
    List<Ride> findByStatusOrderByCreatedAtDesc(RideStatus status);
    
    // Find active ride for driver
    Optional<Ride> findByDriverIdAndStatusIn(String driverId, List<RideStatus> statuses);
    
    // Find active ride for rider
    Optional<Ride> findByRiderIdAndStatusIn(String riderId, List<RideStatus> statuses);
    
    // Count rides by status in time period
    @Query("SELECT COUNT(r) FROM Ride r WHERE r.status = :status AND r.createdAt BETWEEN :startTime AND :endTime")
    long countByStatusAndCreatedAtBetween(@Param("status") RideStatus status, 
                                         @Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
    
    // Find rides in area (simple bounding box)
    @Query("SELECT r FROM Ride r WHERE r.status IN :statuses " +
           "AND r.pickupLat BETWEEN :minLat AND :maxLat " +
           "AND r.pickupLng BETWEEN :minLng AND :maxLng " +
           "ORDER BY r.createdAt DESC")
    List<Ride> findRidesInArea(@Param("statuses") List<RideStatus> statuses,
                              @Param("minLat") Double minLat, @Param("maxLat") Double maxLat,
                              @Param("minLng") Double minLng, @Param("maxLng") Double maxLng);
    
    // Find pending rides (requested but not accepted)
    @Query("SELECT r FROM Ride r WHERE r.status = 'REQUESTED' AND r.createdAt > :cutoffTime ORDER BY r.createdAt ASC")
    List<Ride> findPendingRides(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Statistics queries
    @Query("SELECT AVG(EXTRACT(EPOCH FROM (r.completedAt - r.createdAt))/60) FROM Ride r WHERE r.status = 'COMPLETED' AND r.createdAt > :since")
    Optional<Double> findAverageRideDurationMinutes(@Param("since") LocalDateTime since);
    
    @Query("SELECT AVG(r.fareAmount) FROM Ride r WHERE r.status = 'COMPLETED' AND r.createdAt > :since")
    Optional<Double> findAverageFareAmount(@Param("since") LocalDateTime since);
}
