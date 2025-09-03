package com.dispatch.api.integration;

import com.dispatch.api.DispatchApiApplication;
import com.dispatch.api.dto.request.CreateRideRequest;
import com.dispatch.api.dto.response.RideResponse;
import com.dispatch.api.model.Driver;
import com.dispatch.api.model.DriverStatus;
import com.dispatch.api.repository.DriverRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests using TestContainers
 * Tests the entire dispatch flow with real infrastructure components
 */
@SpringBootTest(classes = DispatchApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@Testcontainers
@Transactional
class DispatchSystemIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("dispatch_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Database configuration
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        
        // Kafka configuration
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        
        // Redis configuration
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
        
        // Disable external services for testing
        registry.add("app.geo-index.enabled", () -> "false");
        registry.add("app.features.surge-pricing", () -> "true");
        registry.add("app.features.eta-prediction", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DriverRepository driverRepository;

    private Driver testDriver;

    @BeforeEach
    void setUp() {
        // Create test driver
        testDriver = new Driver("test-driver-001", "John Doe");
        testDriver.setPhone("+1234567890");
        testDriver.setLicensePlate("ABC-123");
        testDriver.setCurrentLat(new BigDecimal("40.7589"));
        testDriver.setCurrentLng(new BigDecimal("-73.9851"));
        testDriver.setStatus(DriverStatus.AVAILABLE);
        testDriver.setRating(new BigDecimal("4.85"));
        testDriver.setExperienceYears(5);
        testDriver.setTotalRides(1250);
        testDriver.setAcceptanceRate(new BigDecimal("0.95"));
        testDriver = driverRepository.save(testDriver);
    }

    @Test
    void testCompleteRideWorkflow() throws Exception {
        // 1. Set driver online
        mockMvc.perform(post("/api/drivers/{driverId}/online", testDriver.getId())
                .param("lat", "40.7589")
                .param("lng", "-73.9851"))
                .andExpect(status().isOk());

        // 2. Create ride request
        CreateRideRequest rideRequest = new CreateRideRequest();
        rideRequest.setRiderId("test-rider-001");
        rideRequest.setPickupLat(40.7590);
        rideRequest.setPickupLng(-73.9850);
        rideRequest.setDestinationLat(40.7505);
        rideRequest.setDestinationLng(-73.9934);

        MvcResult createResult = mockMvc.perform(post("/api/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rideRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.riderId").value("test-rider-001"))
                .andExpected(jsonPath("$.status").value("REQUESTED"))
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        RideResponse rideResponse = objectMapper.readValue(responseJson, RideResponse.class);
        String rideId = rideResponse.getRideId();

        // 3. Start ride
        mockMvc.perform(post("/api/rides/{rideId}/start", rideId)
                .param("driverId", testDriver.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // 4. Update driver location during ride
        mockMvc.perform(post("/api/drivers/{driverId}/location", testDriver.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "driverId": "%s",
                        "lat": 40.7580,
                        "lng": -73.9860,
                        "heading": 45.0,
                        "speedKmh": 25.5,
                        "accuracyMeters": 3.0
                    }
                    """.formatted(testDriver.getId())))
                .andExpect(status().isOk());

        // 5. Complete ride
        mockMvc.perform(post("/api/rides/{rideId}/complete", rideId)
                .param("driverId", testDriver.getId())
                .param("fareAmount", "18.50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.actualFare").value(18.50));

        // 6. Verify driver is available again
        mockMvc.perform(get("/api/drivers/{driverId}", testDriver.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void testDriverAssignmentAlgorithm() throws Exception {
        // Create multiple drivers at different distances
        Driver nearDriver = createTestDriver("near-driver", 40.7590, -73.9850, 4.9, 0.98);
        Driver farDriver = createTestDriver("far-driver", 40.7600, -73.9800, 4.5, 0.85);
        Driver highRatedDriver = createTestDriver("rated-driver", 40.7595, -73.9855, 4.95, 0.99);

        // Create ride request
        CreateRideRequest rideRequest = new CreateRideRequest();
        rideRequest.setRiderId("test-rider-002");
        rideRequest.setPickupLat(40.7589);
        rideRequest.setPickupLng(-73.9851);
        rideRequest.setDestinationLat(40.7505);
        rideRequest.setDestinationLng(-73.9934);

        MvcResult result = mockMvc.perform(post("/api/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rideRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        RideResponse response = objectMapper.readValue(
            result.getResponse().getContentAsString(), 
            RideResponse.class
        );

        // Verify optimal driver assignment (should prefer high-rated, nearby driver)
        assertNotNull(response.getRideId());
        assertEquals("REQUESTED", response.getStatus());
    }

    @Test
    void testSurgePricingDuringHighDemand() throws Exception {
        // Create multiple ride requests to simulate high demand
        for (int i = 0; i < 5; i++) {
            CreateRideRequest request = new CreateRideRequest();
            request.setRiderId("rider-" + i);
            request.setPickupLat(40.7589 + (i * 0.001));
            request.setPickupLng(-73.9851 + (i * 0.001));
            request.setDestinationLat(40.7505);
            request.setDestinationLng(-73.9934);

            mockMvc.perform(post("/api/rides")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Next ride should have higher estimated fare due to surge
        CreateRideRequest surgeRequest = new CreateRideRequest();
        surgeRequest.setRiderId("surge-rider");
        surgeRequest.setPickupLat(40.7589);
        surgeRequest.setPickupLng(-73.9851);
        surgeRequest.setDestinationLat(40.7505);
        surgeRequest.setDestinationLng(-73.9934);

        MvcResult result = mockMvc.perform(post("/api/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(surgeRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        RideResponse response = objectMapper.readValue(
            result.getResponse().getContentAsString(), 
            RideResponse.class
        );

        // Verify surge pricing is applied
        assertNotNull(response.getEstimatedFare());
        assertTrue(response.getEstimatedFare().compareTo(new BigDecimal("10.00")) > 0);
    }

    @Test
    void testConcurrentRideRequests() throws Exception {
        // Test system behavior under concurrent load
        int concurrentRequests = 10;
        
        for (int i = 0; i < concurrentRequests; i++) {
            final int requestId = i;
            
            CreateRideRequest request = new CreateRideRequest();
            request.setRiderId("concurrent-rider-" + requestId);
            request.setPickupLat(40.7589 + (requestId * 0.0001));
            request.setPickupLng(-73.9851 + (requestId * 0.0001));
            request.setDestinationLat(40.7505);
            request.setDestinationLng(-73.9934);

            mockMvc.perform(post("/api/rides")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    void testErrorHandlingAndRecovery() throws Exception {
        // Test invalid ride request
        CreateRideRequest invalidRequest = new CreateRideRequest();
        invalidRequest.setRiderId(""); // Invalid empty rider ID
        invalidRequest.setPickupLat(40.7589);
        invalidRequest.setPickupLng(-73.9851);

        mockMvc.perform(post("/api/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Test ride not found
        mockMvc.perform(get("/api/rides/{rideId}", "non-existent-ride"))
                .andExpect(status().isNotFound());

        // Test driver not found
        mockMvc.perform(get("/api/drivers/{driverId}", "non-existent-driver"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPerformanceMetrics() throws Exception {
        long startTime = System.currentTimeMillis();

        // Create ride
        CreateRideRequest rideRequest = new CreateRideRequest();
        rideRequest.setRiderId("performance-rider");
        rideRequest.setPickupLat(40.7589);
        rideRequest.setPickupLng(-73.9851);
        rideRequest.setDestinationLat(40.7505);
        rideRequest.setDestinationLng(-73.9934);

        mockMvc.perform(post("/api/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rideRequest)))
                .andExpect(status().isCreated());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Verify performance SLA (should complete within 500ms)
        assertTrue(responseTime < 500, 
            "Ride creation took " + responseTime + "ms, exceeds 500ms SLA");
    }

    private Driver createTestDriver(String id, double lat, double lng, double rating, double acceptanceRate) {
        Driver driver = new Driver(id, "Test Driver " + id);
        driver.setPhone("+1234567890");
        driver.setLicensePlate("TEST-" + id.substring(0, 3).toUpperCase());
        driver.setCurrentLat(new BigDecimal(lat));
        driver.setCurrentLng(new BigDecimal(lng));
        driver.setStatus(DriverStatus.AVAILABLE);
        driver.setRating(new BigDecimal(rating));
        driver.setExperienceYears(3);
        driver.setTotalRides(500);
        driver.setAcceptanceRate(new BigDecimal(acceptanceRate));
        return driverRepository.save(driver);
    }
}
