package com.dispatch.api.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${app.kafka.topics.ride-events}")
    private String rideEventsTopic;
    
    @Value("${app.kafka.topics.driver-locations}")
    private String driverLocationsTopic;
    
    @Value("${app.kafka.topics.ride-assignments}")
    private String rideAssignmentsTopic;
    
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }
    
    @Bean
    public NewTopic rideEventsTopic() {
        return new NewTopic(rideEventsTopic, 3, (short) 1);
    }
    
    @Bean
    public NewTopic driverLocationsTopic() {
        return new NewTopic(driverLocationsTopic, 5, (short) 1);
    }
    
    @Bean
    public NewTopic rideAssignmentsTopic() {
        return new NewTopic(rideAssignmentsTopic, 3, (short) 1);
    }
}
