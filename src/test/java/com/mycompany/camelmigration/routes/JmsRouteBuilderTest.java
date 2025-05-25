package com.mycompany.camelmigration.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;


import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@CamelSpringBootTest
// Use test profile to ensure application-test.yml is loaded (for vm:// broker)
@ActiveProfiles("test") 
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST)
class JmsRouteBuilderTest {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;

    @Test
    void testSendAndReceiveJmsMessage() throws Exception {
        // The jmsConsumerLogEndpoint is the ID we added to the .to(log:...) in the consumer route
        // We will replace this with a mock endpoint for testing.
        AdviceWith.adviceWith(camelContext, "jms-consumer-mytestqueue", a -> {
            // Replace the node with ID "jmsConsumerLogEndpoint" with a mock
            a.weaveById("jmsConsumerLogEndpoint").replace().to("mock:jmsOutput");
        });

        MockEndpoint mockJmsOutput = camelContext.getEndpoint("mock:jmsOutput", MockEndpoint.class);
        String testMessage = "Hello JMS from Camel Test!";
        mockJmsOutput.expectedMessageCount(1);
        mockJmsOutput.expectedBodiesReceived(testMessage);

        // Send a message via the HTTP endpoint that produces to the JMS queue
        String httpResponse = producerTemplate.requestBody(
            "platform-http:/api/sendjms",
            testMessage,
            String.class
        );

        assertEquals("{\"status\":\"Message sent to JMS queue successfully\"}", httpResponse);

        // Assert that the JMS consumer route processed the message
        mockJmsOutput.assertIsSatisfied(5000); // Wait up to 5 seconds for JMS message to be processed
    }
}
