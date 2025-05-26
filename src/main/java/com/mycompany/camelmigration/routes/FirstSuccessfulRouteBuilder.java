package com.mycompany.camelmigration.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FirstSuccessfulRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("platform-http:/invokefirstsuccess?httpMethodRestrict=GET")
            .routeId("invoke-first-successful-http")
            .log(LoggingLevel.INFO, "Received request for FirstSuccessful flow.")
            .setProperty("firstSuccessfulResult", constant(null)) // Initialize property

            // Attempt Route 1 (Agify API)
            .doTry()
                .log(LoggingLevel.INFO, "Attempting Route 1: Agify API (https://api.agify.io?name=meelad)")
                .to("https://api.agify.io?name=meelad&bridgeEndpoint=true&throwExceptionOnFailure=true") // throwExceptionOnFailure=true is default for http component
                .unmarshal().json(JsonLibrary.Jackson, Map.class)
                .log(LoggingLevel.INFO, "Agify API (Route 1) call successful. Payload: ${body}")
                .setProperty("firstSuccessfulResult", body())
            .doCatch(Exception.class)
                .log(LoggingLevel.WARN, "Agify API (Route 1) failed: ${exception.class.name} - ${exception.message}")
                .setProperty("firstSuccessfulResult", constant(null)) // Ensure it's null if this route fails
            .endDoTry()

            // Attempt Route 2 (Ipinfo API) - only if Route 1 failed
            .choice()
                .when(exchangeProperty("firstSuccessfulResult").isNull())
                    .log(LoggingLevel.INFO, "Route 1 (Agify API) failed or was skipped. Attempting Route 2: Ipinfo API.")
                    .doTry()
                        .log(LoggingLevel.INFO, "Attempting Route 2: Ipinfo API (https://ipinfo.io/161.185.160.98/geo)")
                        .to("https://ipinfo.io/161.185.160.98/geo?bridgeEndpoint=true&throwExceptionOnFailure=true")
                        .unmarshal().json(JsonLibrary.Jackson, Map.class)
                        .log(LoggingLevel.INFO, "Ipinfo API (Route 2) call successful. Payload: ${body}")
                        .setProperty("firstSuccessfulResult", body())
                    .doCatch(Exception.class)
                        .log(LoggingLevel.ERROR, "Ipinfo API (Route 2) also failed: ${exception.class.name} - ${exception.message}")
                        // firstSuccessfulResult remains null from previous step or initialization
                    .endDoTry()
            .endChoice()

            // Prepare Final Response
            .choice()
                .when(exchangeProperty("firstSuccessfulResult").isNotNull())
                    .log(LoggingLevel.INFO, "A route was successful. Preparing success response.")
                    .setBody(exchangeProperty("firstSuccessfulResult"))
                    .marshal().json(JsonLibrary.Jackson)
                .otherwise()
                    .log(LoggingLevel.ERROR, "All routes failed in FirstSuccessful flow.")
                    .setBody(constant("{\"error\": \"All routes failed to provide a successful response.\"}"))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(503)) // Service Unavailable, as all attempts failed
            .endChoice()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(LoggingLevel.INFO, "FirstSuccessful flow finished. Response: ${bodyAs(String)}");
    }
}
