package com.mycompany.camelmigration.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.caffeine.CaffeineConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class ObjectStoreRouteBuilder extends RouteBuilder {

    private final String CACHE_NAME = "myObjectStore"; // Define cache name

    @Override
    public void configure() throws Exception {

        // Store Value Flow
        from("platform-http:/os/store?httpMethodRestrict=POST")
            .routeId("objectstore-store-http")
            .log(LoggingLevel.INFO, "Received request to store object. Key: ${header.storeKey}, Value: ${bodyAs(String)}")
            .choice()
                .when(header("storeKey").isNull())
                    .log(LoggingLevel.WARN, "storeKey query parameter is missing.")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                    .setBody(constant("{\"error\": \"Query parameter 'storeKey' is required.\"}"))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .otherwise()
                    .setHeader(CaffeineConstants.KEY, header("storeKey"))
                    .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_PUT))
                    // The message body is implicitly used as the value to be stored
                    .to("caffeine-cache://" + CACHE_NAME)
                    .log(LoggingLevel.INFO, "Successfully stored value for key: ${header.storeKey}")
                    .transform().constant("{\"status\": \"Stored successfully\"}")
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .endChoice();

        // Retrieve Value Flow
        from("platform-http:/os/retrieve?httpMethodRestrict=GET")
            .routeId("objectstore-retrieve-http")
            .log(LoggingLevel.INFO, "Received request to retrieve object for key: ${header.retrieveKey}")
            .choice()
                .when(header("retrieveKey").isNull())
                    .log(LoggingLevel.WARN, "retrieveKey query parameter is missing.")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                    .setBody(constant("{\"error\": \"Query parameter 'retrieveKey' is required.\"}"))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .otherwise()
                    .setHeader(CaffeineConstants.KEY, header("retrieveKey"))
                    .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_GET))
                    .to("caffeine-cache://" + CACHE_NAME)
                    // Log the result of ACTION_GET - the body might be null if key not found
                    .log(LoggingLevel.INFO, "Retrieved from cache for key ${header.retrieveKey}: ${body}")
                    .choice()
                        .when(body().isNull())
                            .log(LoggingLevel.INFO, "Key ${header.retrieveKey} not found in cache.")
                            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                            .transform().constant("{\"error\": \"Key not found\"}")
                        .otherwise()
                            .log(LoggingLevel.INFO, "Key ${header.retrieveKey} found. Value: ${bodyAs(String)}")
                            // Attempt to marshal if the content is complex (e.g. a Map or List previously stored)
                            // If it was stored as a simple string, marshalling might not be needed or could be an issue
                            // depending on what is stored. For now, assume it might be complex.
                            // If the stored object is a simple String, it will be returned as is (after any marshalling if body is Map/List).
                            // If the stored object is a Map/List, it will be marshalled to JSON.
                            // If storing plain strings, this .marshal().json() might wrap it in quotes,
                            // so consider conditional marshalling or ensure consistent stored types.
                            // For now, let's assume stored values could be complex and need JSON representation.
                            .marshal().json(JsonLibrary.Jackson)
                    .endChoice()
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .endChoice();

        // Note on Caffeine Configuration:
        // By default, camel-caffeine-cache uses a default Caffeine configuration.
        // To customize (e.g., size limits, eviction policies), you can:
        // 1. Define a `CaffeineConfiguration` bean in your Spring Boot config.
        //    The component will automatically look for a bean of this type.
        // 2. Configure via `application.yml` using properties like:
        //    camel.component.caffeine-cache.myObjectStore.initial-capacity=100
        //    camel.component.caffeine-cache.myObjectStore.maximum-size=500
        //    camel.component.caffeine-cache.myObjectStore.expire-after-access-time=300s (e.g., "300s", "5m")
        //    (Refer to Camel Caffeine component documentation for specific properties)
        // For this exercise, default configuration is sufficient.
        // A basic configuration example was added to application.yml previously for `appCache`.
        // If `myObjectStore` needs different settings, a specific block can be added:
        // camel.component.caffeine-cache.myObjectStore.maximumSize=200
    }
}
