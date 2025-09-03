package com.dispatch.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class FeatureFlagService {
    
    private static final Logger logger = LoggerFactory.getLogger(FeatureFlagService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${app.feature-flags.api-url}")
    private String featureFlagApiUrl;
    
    @Value("${app.feature-flags.enabled}")
    private boolean featureFlagsEnabled;
    
    public FeatureFlagService() {
        this.restTemplate = new RestTemplate();
    }
    
    public boolean isFeatureEnabled(String featureName, String context) {
        if (!featureFlagsEnabled) {
            // Default behavior when feature flags are disabled
            return getDefaultValue(featureName);
        }
        
        try {
            String url = featureFlagApiUrl + "/flags/" + featureName + "?context=" + context;
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("enabled")) {
                boolean enabled = (Boolean) response.get("enabled");
                logger.debug("Feature flag '{}' for context '{}': {}", featureName, context, enabled);
                return enabled;
            }
            
        } catch (Exception e) {
            logger.warn("Failed to fetch feature flag '{}' for context '{}', using default", 
                       featureName, context, e);
        }
        
        return getDefaultValue(featureName);
    }
    
    public double getFeaturePercentage(String featureName, String context) {
        if (!featureFlagsEnabled) {
            return 0.0;
        }
        
        try {
            String url = featureFlagApiUrl + "/flags/" + featureName + "/percentage?context=" + context;
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("percentage")) {
                double percentage = ((Number) response.get("percentage")).doubleValue();
                logger.debug("Feature flag percentage '{}' for context '{}': {}%", featureName, context, percentage);
                return percentage;
            }
            
        } catch (Exception e) {
            logger.warn("Failed to fetch feature flag percentage '{}' for context '{}', using default", 
                       featureName, context, e);
        }
        
        return 0.0;
    }
    
    private boolean getDefaultValue(String featureName) {
        // Define default values for known features
        return switch (featureName) {
            case "advanced_matching" -> false;
            case "surge_pricing" -> false;
            case "dynamic_eta" -> true;
            case "real_time_tracking" -> true;
            default -> false;
        };
    }
}
