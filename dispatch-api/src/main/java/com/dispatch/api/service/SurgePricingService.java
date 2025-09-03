package com.dispatch.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Advanced dynamic surge pricing service with real-time demand analysis
 * Factors: supply/demand ratio, time of day, weather, events, location density
 */
@Service
public class SurgePricingService {
    
    private static final Logger logger = LoggerFactory.getLogger(SurgePricingService.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final DispatchService dispatchService;
    
    // Base surge pricing parameters
    private static final BigDecimal MIN_SURGE = new BigDecimal("1.0");
    private static final BigDecimal MAX_SURGE = new BigDecimal("5.0");
    private static final BigDecimal BASE_FARE_PER_KM = new BigDecimal("2.50");
    private static final BigDecimal BASE_FARE_MINIMUM = new BigDecimal("8.00");
    private static final BigDecimal TIME_CHARGE_PER_MINUTE = new BigDecimal("0.40");
    
    // Surge triggers
    private static final double HIGH_DEMAND_THRESHOLD = 0.8; // 80% driver utilization
    private static final double CRITICAL_DEMAND_THRESHOLD = 0.95; // 95% driver utilization
    
    public SurgePricingService(RedisTemplate<String, Object> redisTemplate, 
                              DispatchService dispatchService) {
        this.redisTemplate = redisTemplate;
        this.dispatchService = dispatchService;
    }
    
    /**
     * Calculate dynamic surge multiplier for a specific location
     */
    @Cacheable(value = "surge-cache", key = "#lat + '-' + #lng", unless = "#result.compareTo(new java.math.BigDecimal('1.0')) <= 0")
    public BigDecimal calculateSurgeMultiplier(double lat, double lng) {
        try {
            // 1. Calculate supply/demand ratio
            double supplyDemandRatio = calculateSupplyDemandRatio(lat, lng);
            
            // 2. Time-based surge factors
            double timeMultiplier = getTimeBasedMultiplier();
            
            // 3. Weather impact
            double weatherMultiplier = getWeatherSurgeMultiplier();
            
            // 4. Event-based surge
            double eventMultiplier = getEventSurgeMultiplier(lat, lng);
            
            // 5. Location density factor
            double densityMultiplier = getLocationDensityMultiplier(lat, lng);
            
            // 6. Historical demand patterns
            double historicalMultiplier = getHistoricalDemandMultiplier(lat, lng);
            
            // Combine all factors using weighted algorithm
            BigDecimal surgeFactor = calculateWeightedSurge(
                supplyDemandRatio, timeMultiplier, weatherMultiplier,
                eventMultiplier, densityMultiplier, historicalMultiplier
            );
            
            // Apply smoothing to avoid dramatic price swings
            BigDecimal smoothedSurge = applySurgeSmoothing(lat, lng, surgeFactor);
            
            // Ensure within bounds
            BigDecimal finalSurge = smoothedSurge.max(MIN_SURGE).min(MAX_SURGE);
            
            // Cache the result for 2 minutes
            cacheSurgeMultiplier(lat, lng, finalSurge);
            
            logger.debug("Surge calculation for ({}, {}): supply/demand={}, time={}, weather={}, events={}, density={}, historical={}, final={}",
                lat, lng, supplyDemandRatio, timeMultiplier, weatherMultiplier, 
                eventMultiplier, densityMultiplier, historicalMultiplier, finalSurge);
            
            return finalSurge;
            
        } catch (Exception e) {
            logger.error("Error calculating surge multiplier for location ({}, {})", lat, lng, e);
            return MIN_SURGE; // Fallback to base pricing
        }
    }
    
    /**
     * Calculate comprehensive ride fare with surge pricing
     */
    public BigDecimal calculateFare(double pickupLat, double pickupLng, 
                                   double destLat, double destLng, 
                                   int estimatedMinutes, String vehicleType) {
        
        // Calculate distance
        double distance = calculateDistance(pickupLat, pickupLng, destLat, destLng);
        
        // Get surge multiplier for pickup location
        BigDecimal surgeMultiplier = calculateSurgeMultiplier(pickupLat, pickupLng);
        
        // Base fare calculation
        BigDecimal distanceFare = BASE_FARE_PER_KM.multiply(BigDecimal.valueOf(distance));
        BigDecimal timeFare = TIME_CHARGE_PER_MINUTE.multiply(BigDecimal.valueOf(estimatedMinutes));
        BigDecimal baseFare = distanceFare.add(timeFare).max(BASE_FARE_MINIMUM);
        
        // Vehicle type multiplier
        BigDecimal vehicleMultiplier = getVehicleTypeMultiplier(vehicleType);
        
        // Apply surge and vehicle multipliers
        BigDecimal finalFare = baseFare
            .multiply(surgeMultiplier)
            .multiply(vehicleMultiplier)
            .setScale(2, RoundingMode.HALF_UP);
        
        logger.info("Fare calculation: distance={:.2f}km, time={}min, base=${}, surge={}, vehicle={}, final=${}",
            distance, estimatedMinutes, baseFare, surgeMultiplier, vehicleMultiplier, finalFare);
        
        return finalFare;
    }
    
    private double calculateSupplyDemandRatio(double lat, double lng) {
        // Get available drivers in area (2km radius)
        int availableDrivers = dispatchService.getNearbyDriversCount(lat, lng, 2000);
        
        // Get pending ride requests in area
        int pendingRequests = getPendingRequestsInArea(lat, lng, 2000);
        
        // Calculate utilization rate
        double utilization = pendingRequests / Math.max(1.0, availableDrivers + pendingRequests);
        
        // Convert to surge factor (higher utilization = higher surge)
        if (utilization >= CRITICAL_DEMAND_THRESHOLD) {
            return 3.0; // Critical surge
        } else if (utilization >= HIGH_DEMAND_THRESHOLD) {
            return 1.5 + (utilization - HIGH_DEMAND_THRESHOLD) * 10; // Gradual increase
        } else {
            return 1.0; // No surge from supply/demand
        }
    }
    
    private double getTimeBasedMultiplier() {
        LocalTime now = LocalTime.now();
        
        // Peak hours surge
        if (isRushHour(now)) {
            return 1.8;
        } else if (isLateNight(now)) {
            return 1.5; // Late night premium
        } else if (isWeekend()) {
            return 1.2; // Weekend premium
        } else {
            return 1.0;
        }
    }
    
    private boolean isRushHour(LocalTime time) {
        return (time.isAfter(LocalTime.of(7, 0)) && time.isBefore(LocalTime.of(10, 0))) ||
               (time.isAfter(LocalTime.of(17, 0)) && time.isBefore(LocalTime.of(20, 0)));
    }
    
    private boolean isLateNight(LocalTime time) {
        return time.isAfter(LocalTime.of(22, 0)) || time.isBefore(LocalTime.of(6, 0));
    }
    
    private boolean isWeekend() {
        LocalDateTime now = LocalDateTime.now();
        return now.getDayOfWeek().getValue() >= 6; // Saturday or Sunday
    }
    
    private double getWeatherSurgeMultiplier() {
        // Simulate weather API integration
        double weatherRoll = ThreadLocalRandom.current().nextDouble();
        
        if (weatherRoll < 0.05) return 2.0; // Severe weather (storm, snow)
        if (weatherRoll < 0.15) return 1.5; // Bad weather (heavy rain)
        if (weatherRoll < 0.30) return 1.2; // Light rain
        return 1.0; // Clear weather
    }
    
    private double getEventSurgeMultiplier(double lat, double lng) {
        // Check for major events near the location
        // In production, this would integrate with events APIs
        
        // Simulate major venues and events
        if (isNearMajorVenue(lat, lng)) {
            // Check if event is happening (simplified)
            LocalTime now = LocalTime.now();
            if (isEventTime(now)) {
                return 2.5; // Major event surge
            }
        }
        
        // Check for airport proximity (always has some premium)
        if (isNearAirport(lat, lng)) {
            return 1.3;
        }
        
        return 1.0;
    }
    
    private boolean isNearMajorVenue(double lat, double lng) {
        // Example: Madison Square Garden area
        return Math.abs(lat - 40.7505) < 0.005 && Math.abs(lng + 73.9934) < 0.005;
    }
    
    private boolean isNearAirport(double lat, double lng) {
        // Example: JFK Airport area
        return Math.abs(lat - 40.6413) < 0.01 && Math.abs(lng + 73.7781) < 0.01;
    }
    
    private boolean isEventTime(LocalTime time) {
        // Events typically in evening
        return time.isAfter(LocalTime.of(18, 0)) && time.isBefore(LocalTime.of(23, 0));
    }
    
    private double getLocationDensityMultiplier(double lat, double lng) {
        // Urban density affects base demand
        // Manhattan-like area has higher baseline demand
        if (isHighDensityArea(lat, lng)) {
            return 1.2;
        }
        return 1.0;
    }
    
    private boolean isHighDensityArea(double lat, double lng) {
        // Manhattan coordinates range
        return lat >= 40.7 && lat <= 40.8 && lng >= -74.0 && lng <= -73.9;
    }
    
    private double getHistoricalDemandMultiplier(double lat, double lng) {
        // Get historical demand pattern for this location and time
        String cacheKey = String.format("historical_demand:%.3f:%.3f:%d:%d", 
            lat, lng, LocalDateTime.now().getHour(), LocalDateTime.now().getDayOfWeek().getValue());
        
        Double cachedMultiplier = (Double) redisTemplate.opsForValue().get(cacheKey);
        if (cachedMultiplier != null) {
            return cachedMultiplier;
        }
        
        // Simulate historical analysis (in production, this would query actual data)
        double baseHistorical = 1.0 + ThreadLocalRandom.current().nextGaussian() * 0.1;
        double clampedHistorical = Math.max(0.8, Math.min(1.4, baseHistorical));
        
        // Cache for 1 hour
        redisTemplate.opsForValue().set(cacheKey, clampedHistorical, java.time.Duration.ofHours(1));
        
        return clampedHistorical;
    }
    
    private BigDecimal calculateWeightedSurge(double supplyDemand, double time, double weather,
                                            double events, double density, double historical) {
        // Weighted combination of all factors
        double weightedSum = 
            supplyDemand * 0.40 +    // Supply/demand is most important
            time * 0.25 +            // Time-based patterns
            weather * 0.15 +         // Weather impact
            events * 0.10 +          // Special events
            density * 0.05 +         // Location density
            historical * 0.05;       // Historical patterns
        
        return BigDecimal.valueOf(weightedSum).setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal applySurgeSmoothing(double lat, double lng, BigDecimal newSurge) {
        // Get previous surge value
        String cacheKey = String.format("surge:%.3f:%.3f", lat, lng);
        BigDecimal previousSurge = (BigDecimal) redisTemplate.opsForValue().get(cacheKey);
        
        if (previousSurge == null) {
            return newSurge; // No previous value, use new surge
        }
        
        // Limit surge changes to prevent dramatic swings
        BigDecimal maxChange = new BigDecimal("0.5"); // Max 0.5x change per update
        BigDecimal difference = newSurge.subtract(previousSurge);
        
        if (difference.abs().compareTo(maxChange) > 0) {
            // Limit the change
            BigDecimal limitedChange = difference.signum() > 0 ? maxChange : maxChange.negate();
            return previousSurge.add(limitedChange);
        }
        
        return newSurge;
    }
    
    private void cacheSurgeMultiplier(double lat, double lng, BigDecimal surge) {
        String cacheKey = String.format("surge:%.3f:%.3f", lat, lng);
        redisTemplate.opsForValue().set(cacheKey, surge, java.time.Duration.ofMinutes(2));
    }
    
    private int getPendingRequestsInArea(double lat, double lng, int radiusMeters) {
        // In production, this would query the database for pending rides in the area
        // For simulation, return a random number based on area characteristics
        
        if (isHighDensityArea(lat, lng)) {
            return ThreadLocalRandom.current().nextInt(5, 20); // High demand area
        } else {
            return ThreadLocalRandom.current().nextInt(0, 8);  // Normal area
        }
    }
    
    private BigDecimal getVehicleTypeMultiplier(String vehicleType) {
        return switch (vehicleType.toUpperCase()) {
            case "PREMIUM" -> new BigDecimal("1.5");
            case "SUV" -> new BigDecimal("1.8");
            case "LUXURY" -> new BigDecimal("2.5");
            case "STANDARD" -> new BigDecimal("1.0");
            default -> new BigDecimal("1.0");
        };
    }
    
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371; // Earth's radius in kilometers
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Get current surge level for display to users
     */
    public String getSurgeLevelDescription(BigDecimal surgeMultiplier) {
        double surge = surgeMultiplier.doubleValue();
        
        if (surge >= 4.0) return "Very High Demand";
        if (surge >= 2.5) return "High Demand";
        if (surge >= 1.8) return "Increased Demand";
        if (surge >= 1.3) return "Moderate Demand";
        return "Normal Pricing";
    }
    
    /**
     * Predict surge for next hour (for rider planning)
     */
    public BigDecimal predictSurgeInOneHour(double lat, double lng) {
        // Simple prediction based on historical patterns
        LocalTime futureTime = LocalTime.now().plusHours(1);
        
        // Adjust for known patterns
        double prediction = calculateSurgeMultiplier(lat, lng).doubleValue();
        
        if (futureTime.getHour() == 17 || futureTime.getHour() == 8) {
            prediction *= 1.2; // Rush hour approaching
        } else if (futureTime.getHour() >= 22) {
            prediction *= 1.1; // Late night premium
        }
        
        return BigDecimal.valueOf(prediction).setScale(2, RoundingMode.HALF_UP);
    }
}
