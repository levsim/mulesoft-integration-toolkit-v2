package com.mycompany.camelmigration.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@CamelSpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST)
class ScatterGatherRouteBuilderTest {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext; // Not strictly needed for this test, but good practice

    @Autowired
    private ObjectMapper objectMapper; // Spring Boot provides an ObjectMapper

    @Test
    void testScatterGather101Endpoint() throws Exception {
        String responseJson = producerTemplate.requestBody(
            "platform-http:/api/scattergather101",
            null, // No body for GET request
            String.class
        );

        assertNotNull(responseJson, "Response JSON should not be null");
        assertTrue(responseJson.startsWith("[") && responseJson.endsWith("]"), "Response should be a JSON array");

        // Unmarshal the JSON response to a List of Maps
        List<Map<String, Object>> responseList = objectMapper.readValue(responseJson, new TypeReference<List<Map<String, Object>>>() {});

        assertNotNull(responseList, "Parsed response list should not be null");
        assertEquals(3, responseList.size(), "There should be 3 elements in the aggregated list (from 3 routes)");

        // Verify the content of each map (order might vary due to parallel processing, so check presence)
        // Route 1's expected payload
        Map<String, Object> expectedPayload1 = Map.of(
            "payload01Name", "Manju",
            "payload01ID", 1,
            "payload01Location", "INDIA",
            "payload01Salary", 2000
        );
        // Route 2's expected payload
        Map<String, Object> expectedPayload2 = Map.of(
            "payload02Name", "Peter",
            "payload02ID", 2,
            "payload02Location", "UK",
            "payload02Salary", 1000
        );
        // Route 3's expected payload
        Map<String, Object> expectedPayload3 = Map.of(
            "payload03Name", "Rahul",
            "payload03ID", 3,
            "payload03Location", "USA",
            "payload03Salary", 3000
        );

        // The ScatterGatherAggregationBean's aggregateFromListOfMaps method currently returns the list of maps as is.
        // So we expect the list to contain these three maps.
        assertTrue(responseList.contains(expectedPayload1), "Response list should contain payload1");
        assertTrue(responseList.contains(expectedPayload2), "Response list should contain payload2");
        assertTrue(responseList.contains(expectedPayload3), "Response list should contain payload3");
    }
}
