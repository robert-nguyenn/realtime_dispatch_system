package com.dispatch.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    @Value("${app.security.jwt.secret:dispatch-system-super-secret-key-for-jwt-token-generation}")
    private String jwtSecret;
    
    @Value("${app.security.jwt.expiration:86400}") // 24 hours
    private int jwtExpirationInSeconds;
    
    private SecretKey secretKey;
    
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    public String generateToken(String userId, String userType, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(jwtExpirationInSeconds, ChronoUnit.SECONDS);
        
        Map<String, Object> tokenClaims = new HashMap<>(claims);
        tokenClaims.put("user_id", userId);
        tokenClaims.put("user_type", userType);
        tokenClaims.put("iat", now.getEpochSecond());
        
        return Jwts.builder()
                .setClaims(tokenClaims)
                .setSubject(userId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }
    
    public String generateDriverToken(String driverId, String driverLicense, String vehicleId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("driver_license", driverLicense);
        claims.put("vehicle_id", vehicleId);
        claims.put("permissions", new String[]{"accept_rides", "update_location", "complete_rides"});
        
        return generateToken(driverId, "DRIVER", claims);
    }
    
    public String generateRiderToken(String riderId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("permissions", new String[]{"request_rides", "view_rides", "rate_drivers"});
        
        return generateToken(riderId, "RIDER", claims);
    }
    
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }
    
    public String getUserTypeFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.get("user_type", String.class);
    }
    
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty");
        }
        return false;
    }
    
    public boolean hasPermission(String token, String permission) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            @SuppressWarnings("unchecked")
            java.util.List<String> permissions = (java.util.List<String>) claims.get("permissions");
            
            return permissions != null && permissions.contains(permission);
        } catch (Exception e) {
            logger.error("Error checking permission {}", permission, e);
            return false;
        }
    }
    
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
