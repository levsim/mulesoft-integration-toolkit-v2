package com.mycompany.camelmigration.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@CamelSpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST)
class DatabaseRouteBuilderTest {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;

    @Test
    void testSelectFlow_Success() throws Exception {
        AdviceWith.adviceWith(camelContext, "select-db-http", a -> {
            // Replace the SQL endpoint with a mock or a processor
            a.weaveByToUri("sql:ignored?dataSource=#dataSource")
                .replace().process(exchange -> {
                    // Simulate a database response for city=TestCity
                    Map<String, Object> row = new HashMap<>();
                    row.put("ID", 1);
                    row.put("City", "TestCity");
                    row.put("Tradition", "TestTradition");
                    exchange.getIn().setBody(Collections.singletonList(row));
                });
        });

        String city = "TestCity";
        // platform-http uses headers for query parameters
        Map<String, Object> headers = new HashMap<>();
        headers.put("city", city);

        String response = producerTemplate.requestBodyAndHeaders(
            "platform-http:/api/select?city=" + city, // URI can also carry query params
            null, 
            headers, 
            String.class
        );

        String expectedJson = "[{\"ID\":1,\"City\":\"TestCity\",\"Tradition\":\"TestTradition\"}]";
        assertEquals(expectedJson, response);
    }

    @Test
    void testSelectFlow_MissingCityParam() throws Exception {
        Exchange responseExchange = producerTemplate.request(
            "platform-http:/api/select", 
            exchange -> exchange.getIn().setBody(null) // No body for GET
        );

        assertEquals(400, responseExchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));
        String responseBody = responseExchange.getIn().getBody(String.class);
        assertTrue(responseBody.contains("Query parameter 'city' is required."));
    }

    @Test
    void testInsertFlow_Success() throws Exception {
        AdviceWith.adviceWith(camelContext, "insert-db-http", a -> {
            a.weaveByToUri("sql:ignored?dataSource=#dataSource")
                .replace().process(exchange -> {
                    // Simulate successful DB insert (e.g., by checking the input query)
                    String query = exchange.getIn().getBody(String.class);
                    assertTrue(query.contains("INSERT INTO traditions"));
                    assertTrue(query.contains("TestCityInsert"));
                    // Set a mock update count or just let it proceed if no specific body is expected from SQL for insert
                    exchange.getIn().setBody(1); // Simulate 1 row affected
                });
        });

        Map<String, Object> traditionToInsert = new HashMap<>();
        traditionToInsert.put("id", 100);
        traditionToInsert.put("city", "TestCityInsert");
        traditionToInsert.put("tradition", "New Tradition");

        String response = producerTemplate.requestBody(
            "platform-http:/api/insertdb", 
            traditionToInsert, 
            String.class
        );

        assertEquals("{\"status\":\"Inserted OK\"}", response);
    }
    
    @Test
    void testUpdateFlow_Success() throws Exception {
        AdviceWith.adviceWith(camelContext, "update-db-http", a -> {
            a.weaveByToUri("sql:ignored?dataSource=#dataSource")
                .replace().process(exchange -> {
                    String query = exchange.getIn().getBody(String.class);
                    assertTrue(query.contains("UPDATE traditions SET Tradition = 'UpdatedTradition' WHERE ID = 200"));
                    exchange.getIn().setBody(1); // Simulate 1 row affected
                });
        });

        Map<String, Object> traditionToUpdate = new HashMap<>();
        traditionToUpdate.put("id", 200);
        traditionToUpdate.put("tradition", "UpdatedTradition");

        String response = producerTemplate.requestBody(
            "platform-http:/api/updatedb",
            traditionToUpdate,
            String.class
        );
        assertEquals("{\"status\":\"Updated OK\"}", response);
    }

    @Test
    void testDeleteFlow_Success() throws Exception {
        AdviceWith.adviceWith(camelContext, "delete-db-http", a -> {
            a.weaveByToUri("sql:ignored?dataSource=#dataSource")
                .replace().process(exchange -> {
                    String query = exchange.getIn().getBody(String.class);
                    assertTrue(query.contains("DELETE FROM traditions WHERE ID = 300"));
                    exchange.getIn().setBody(1); // Simulate 1 row affected
                });
        });
        
        String deleteId = "300";
        Map<String, Object> headers = new HashMap<>();
        headers.put("id", deleteId);

        String response = producerTemplate.requestBodyAndHeaders(
            "platform-http:/api/deletedb?id=" + deleteId,
            null,
            headers,
            String.class
        );
        assertEquals("{\"status\":\"Deleted OK\"}", response);
    }

    @Test
    void testDeleteFlow_MissingIdParam() throws Exception {
         Exchange responseExchange = producerTemplate.request(
            "platform-http:/api/deletedb",
            exchange -> exchange.getIn().setBody(null)
        );

        assertEquals(400, responseExchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));
        String responseBody = responseExchange.getIn().getBody(String.class);
        assertTrue(responseBody.contains("Query parameter 'id' is required for delete."));
    }
}
