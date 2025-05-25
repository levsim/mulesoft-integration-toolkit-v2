package com.mycompany.camelmigration.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.caffeine.cache.CaffeineCacheComponent;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.github.benmanes.caffeine.cache.Cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@CamelSpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST)
class ObjectStoreRouteBuilderTest {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;
    
    // Helper to clear cache before each test
    @BeforeEach
    void clearCache() {
        CaffeineCacheComponent cacheComponent = camelContext.getComponent("caffeine-cache", CaffeineCacheComponent.class);
        Cache<Object, Object> cache = cacheComponent.getCaches().get("myObjectStore"); // "myObjectStore" is the cache name used in the route
        if (cache != null) {
            cache.invalidateAll();
        }
    }


    @Test
    void testStoreAndRetrieve_Success() {
        String storeKey = "testKey1";
        String storeValue = "{\"message\":\"Hello Camel ObjectStore!\"}"; // Storing as a JSON string

        // Store value
        String storeResponse = producerTemplate.requestBodyAndHeader(
            "platform-http:/api/os/store?storeKey=" + storeKey, 
            storeValue, 
            "Content-Type", "application/json", // Assuming the body is JSON
            String.class
        );
        assertEquals("{\"status\":\"Stored successfully\"}", storeResponse);

        // Retrieve value
        String retrieveResponse = producerTemplate.requestBodyAndHeader(
            "platform-http:/api/os/retrieve?retrieveKey=" + storeKey, 
            null, 
            "retrieveKey", storeKey, // Also sending as header for clarity, though URI param is used by route
            String.class
        );
        // The value was stored as a JSON string, so retrieving it should return that JSON string.
        // The route then marshals this string body to JSON, which means it will be a JSON string *within* a JSON string if not careful.
        // Let's check the bean: if it's a simple string, no marshalling is needed.
        // The route has .marshal().json() - this will wrap a plain string in quotes.
        // So, if "Hello" is stored, retrieval will result in "\"Hello\"" as JSON.
        // If `{"message":"..."}` is stored, retrieval will be `"{\"message\":\"...\"}"`
        // To test accurately, what is expected is the JSON representation of the stored string.
        assertEquals("\"" + storeValue.replace("\"", "\\\"") + "\"", retrieveResponse, 
            "Retrieved value should be the JSON representation of the stored JSON string.");
    }
    
    @Test
    void testStoreAndRetrieve_ComplexObject() {
        String storeKey = "complexKey";
        Map<String, Object> storeValue = Map.of("name", "Camel", "type", "Integration");

        // Store value (Camel will convert Map to JSON with platform-http if Content-Type is set)
        Exchange storeExchange = producerTemplate.request(
            "platform-http:/api/os/store?storeKey=" + storeKey, 
            exchange -> {
                exchange.getIn().setBody(storeValue);
                exchange.getIn().setHeader("Content-Type", "application/json");
            }
        );
        String storeResponse = storeExchange.getIn().getBody(String.class);
        assertEquals("{\"status\":\"Stored successfully\"}", storeResponse);

        // Retrieve value
        String retrieveResponse = producerTemplate.requestBodyAndHeader(
            "platform-http:/api/os/retrieve?retrieveKey=" + storeKey, 
            null, 
            "retrieveKey", storeKey,
            String.class
        );
        // Expecting the Map to be returned as a JSON string
        assertEquals("{\"name\":\"Camel\",\"type\":\"Integration\"}", retrieveResponse);
    }


    @Test
    void testRetrieve_NotFound() {
        String retrieveKey = "nonExistentKey";

        Exchange retrieveExchange = producerTemplate.request(
            "platform-http:/api/os/retrieve?retrieveKey=" + retrieveKey,
            exchange -> {}
        );

        assertEquals(404, retrieveExchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));
        String responseBody = retrieveExchange.getIn().getBody(String.class);
        assertEquals("{\"error\":\"Key not found\"}", responseBody);
    }

    @Test
    void testStore_MissingKey() {
         Exchange storeExchange = producerTemplate.request(
            "platform-http:/api/os/store", // No storeKey
            exchange -> exchange.getIn().setBody("some value")
        );
        assertEquals(400, storeExchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));
        assertTrue(storeExchange.getIn().getBody(String.class).contains("Query parameter 'storeKey' is required."));
    }

    @Test
    void testRetrieve_MissingKey() {
        Exchange retrieveExchange = producerTemplate.request(
            "platform-http:/api/os/retrieve", // No retrieveKey
            exchange -> {}
        );
        assertEquals(400, retrieveExchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));
        assertTrue(retrieveExchange.getIn().getBody(String.class).contains("Query parameter 'retrieveKey' is required."));
    }
}
