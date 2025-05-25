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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@CamelSpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST)
class HttpConfigRouteBuilderTest {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;

    @Test
    void testAsyncChoiceFlow_SuccessPath_IpMatched() throws Exception {
        // Advice the subflow-ipinfo-http-poc to return a specific IP
        AdviceWith.adviceWith(camelContext, "subflow-ipinfo-http-poc", a -> {
            a.weaveByToUri("https://ipinfo.io/161.185.160.99/geo?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .replace().process(exchange -> {
                    Map<String, Object> mockIpInfoResponse = new HashMap<>();
                    mockIpInfoResponse.put("ip", "161.185.160.99"); // IP that matches the 'when' condition
                    mockIpInfoResponse.put("city", "MockCity");
                    exchange.getIn().setBody(mockIpInfoResponse);
                });
        });
        
        // Advice the subflow-abstract-flow-async to ensure it provides the expected output
        AdviceWith.adviceWith(camelContext, "subflow-abstract-flow-async", a -> {
             // No need to change its behavior for this test, just ensure it's called
        });

        // Mock the end of the SEDA route to verify its final payload
        MockEndpoint mockSedaEnd = camelContext.getEndpoint("mock:sedaEnd", MockEndpoint.class);
        AdviceWith.adviceWith(camelContext, "seda-process-async-choice", a -> {
            // Replace the final log with a mock endpoint
            a.weaveByToString(".*Async processing completed.*").replace().to(mockSedaEnd);
        });
        
        mockSedaEnd.expectedMessageCount(1);
        // Expected JSON from the "when" branch after marshalling
        mockSedaEnd.expectedBodiesReceived("{\"abstractName\":\"Test\",\"abstractCity\":\"TestCity\",\"abstractCountry\":\"TestCountry\"}");


        // Test the initial HTTP response
        String initialResponse = producerTemplate.requestBody(
            "platform-http:/api/asyncchoice", 
            "test body", 
            String.class
        );
        assertEquals("{\"status\":\"Request for asyncchoice received and is being processed asynchronously.\"}", initialResponse);
        
        // Assert that the SEDA route completed and produced the expected final payload
        mockSedaEnd.assertIsSatisfied(5000); // Wait up to 5 seconds
    }

    @Test
    void testAsyncChoiceFlow_OtherwisePath() throws Exception {
        // Advice the subflow-ipinfo-http-poc to return an IP that doesn't match the 'when' condition
        AdviceWith.adviceWith(camelContext, "subflow-ipinfo-http-poc", a -> {
            a.weaveByToUri("https://ipinfo.io/161.185.160.99/geo?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .replace().process(exchange -> {
                    Map<String, Object> mockIpInfoResponse = new HashMap<>();
                    mockIpInfoResponse.put("ip", "1.2.3.4"); // Different IP
                    mockIpInfoResponse.put("city", "OtherCity");
                    exchange.getIn().setBody(mockIpInfoResponse);
                });
        });

        MockEndpoint mockSedaEnd = camelContext.getEndpoint("mock:sedaEnd", MockEndpoint.class);
        AdviceWith.adviceWith(camelContext, "seda-process-async-choice", a -> {
            a.weaveByToString(".*Async processing completed.*").replace().to(mockSedaEnd);
        });

        mockSedaEnd.expectedMessageCount(1);
        // Expected JSON string "default value" from the 'otherwise' branch
        mockSedaEnd.expectedBodiesReceived("\"default value\""); 


        String initialResponse = producerTemplate.requestBody(
            "platform-http:/api/asyncchoice", 
            "test body", 
            String.class
        );
        assertEquals("{\"status\":\"Request for asyncchoice received and is being processed asynchronously.\"}", initialResponse);

        mockSedaEnd.assertIsSatisfied(5000);
    }
}
