package com.mycompany.camelmigration.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class JmsRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // JMS Producer Flow
        from("platform-http:/sendjms?httpMethodRestrict=POST")
            .routeId("jms-producer-http")
            .log(LoggingLevel.INFO, "Received HTTP POST request to send to JMS queue myTestQueue. Body: ${bodyAs(String)}")
            // Send the message body to the ActiveMQ queue
            .to("jms:queue:myTestQueue")
            .log(LoggingLevel.INFO, "Message sent to JMS queue myTestQueue.")
            // Set a success response
            .transform().constant("{\"status\": \"Message sent to JMS queue successfully\"}")
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"));

        // JMS Consumer Flow
        from("jms:queue:myTestQueue")
            .routeId("jms-consumer-mytestqueue")
            .log(LoggingLevel.INFO, "Received from JMS queue myTestQueue: ${bodyAs(String)}")
            // Optional: Further processing or sending to another log/endpoint
            .to("log:jmsMessageOutput?showBody=true&showHeaders=true").id("jmsConsumerLogEndpoint");

        // Note on JMS ConnectionFactory:
        // Spring Boot auto-configures a JmsConnectionFactory when the
        // spring-boot-starter-activemq dependency is present and properties
        // like spring.activemq.broker-url are defined in application.yml.
        // For basic scenarios, this is sufficient.
        // For advanced configurations (e.g., connection pooling with activemq-pool,
        // specific redelivery policies not configurable via URI options),
        // a custom jakarta.jms.ConnectionFactory bean can be defined in a @Configuration class.
    }
}
