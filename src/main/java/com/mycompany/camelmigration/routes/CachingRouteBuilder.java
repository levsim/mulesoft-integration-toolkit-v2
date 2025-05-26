package com.mycompany.camelmigration.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.caffeine.CaffeineConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CachingRouteBuilder extends RouteBuilder {

    private final String CACHE_NAME = "myObjectStore"; // Using the same cache as ObjectStoreRouteBuilder for this example

    @Override
    public void configure() throws Exception {

        // Route implementing the logic from Ipinfo_Partofcache flow in cahcePoc.xml
        from("platform-http:/cacheipinfo/{passIPAddr}/{name}?httpMethodRestrict=GET")
            .routeId("cache-ipinfo-http")
            .log(LoggingLevel.INFO, "Received request for /cacheipinfo/${header.passIPAddr}/${header.name}")

            // Conditional Caching Logic (equivalent to filterExpression)
            .choice()
                .when(simple("${header.passIPAddr} == '162.185.160.93'")) // Condition to bypass cache
                    .log(LoggingLevel.INFO, "Caching bypassed for IP: ${header.passIPAddr}. Fetching directly.")
                    // HTTP Request (same as in cache miss path)
                    .setHeader("originalBody", body()) // Save original body if any, though for GET it's usually null
                    .toD("https://ipinfo.io/${header.passIPAddr}/${header.name}?bridgeEndpoint=true&throwExceptionOnFailure=true")
                    .unmarshal().json(JsonLibrary.Jackson, Map.class) // Assuming response is JSON
                    .log(LoggingLevel.INFO, "Direct fetch successful for bypassed IP. Payload: ${body}")
                    // Body is now the result of the HTTP call
                .otherwise() // Caching Active Path
                    .log(LoggingLevel.INFO, "Caching active for IP: ${header.passIPAddr}.")
                    .setHeader(CaffeineConstants.KEY, header("passIPAddr"))
                    .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_GET))
                    .to("caffeine-cache://" + CACHE_NAME)
                    // ACTION_HAS_RESULT header is set by caffeine-cache to true if element was found, false otherwise
                    .log(LoggingLevel.DEBUG, "Cache lookup for key ${header.passIPAddr}. Found in cache: ${header.CamelCaffeineActionHasResult}, Body: ${bodyAs(String)}")

                    .choice()
                        .when(header(CaffeineConstants.ACTION_HAS_RESULT).isEqualTo(true)) // Cache Hit
                            .log(LoggingLevel.INFO, "Cache hit for key: ${header.passIPAddr}. Using cached value: ${bodyAs(String)}")
                            // Body is already the cached value (potentially a Map if stored as such, or String)
                            // If it was stored as a Map, it will be a Map here. If String, it will be String.
                        .otherwise() // Cache Miss
                            .log(LoggingLevel.INFO, "Cache miss for key: ${header.passIPAddr}. Fetching from source.")
                            // HTTP Request (On Cache Miss)
                            .setHeader("originalBody", body()) // Save current body (which is likely null from cache miss)
                            .toD("https://ipinfo.io/${header.passIPAddr}/${header.name}?bridgeEndpoint=true&throwExceptionOnFailure=true")
                            .unmarshal().json(JsonLibrary.Jackson, Map.class) // Assuming response is JSON, parse to Map
                            .log(LoggingLevel.INFO, "Fetched from source for key ${header.passIPAddr}. Payload: ${body}")

                            // Store the fetched result in cache
                            // Key is already in CaffeineConstants.KEY header from the GET attempt
                            .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_PUT))
                            // The body (result of HTTP call, which is a Map now) will be stored
                            .to("caffeine-cache://" + CACHE_NAME)
                            .log(LoggingLevel.INFO, "Stored new value in cache for key: ${header.passIPAddr}")
                            // Body is still the result of the HTTP call (the Map)
                    .endChoice()
            .endChoice()

            // Final Payload Transformation (ensuring output is JSON)
            // If body is already a Map (from cache hit or direct fetch), this will marshal it.
            // If body is a String (e.g. from a simpler cache store), this will make it a JSON string.
            .marshal().json(JsonLibrary.Jackson)
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(LoggingLevel.INFO, "Final response for /cacheipinfo: ${bodyAs(String)}");
    }
}
