package com.mycompany.camelmigration.routes;

import com.mycompany.camelmigration.beans.ScatterGatherAggregationBean;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ScatterGatherRouteBuilder extends RouteBuilder {

    @Autowired
    private ScatterGatherAggregationBean scatterGatherAggregationBean;

    @Override
    public void configure() throws Exception {

        // Main flow for scatter-gather demonstration
        from("platform-http:/scattergather101?httpMethodRestrict=GET")
            .routeId("scatter-gather-101-http")
            .log("Received request on /scattergather101 (thread: ${threadName})")
            
            // Scatter-Gather EIP
            // This will call the direct routes and collect their results into a List<Object> (List of Maps in this case)
            .multicast() // Using multicast as a common way to send to multiple endpoints and aggregate
                .parallelProcessing() // Process routes in parallel
                .aggregationStrategy((oldExchange, newExchange) -> { // Simple list aggregation
                    Object newBody = newExchange.getIn().getBody();
                    if (oldExchange == null) {
                        java.util.ArrayList<Object> list = new java.util.ArrayList<>();
                        list.add(newBody);
                        newExchange.getIn().setBody(list);
                        return newExchange;
                    } else {
                        @SuppressWarnings("unchecked")
                        java.util.ArrayList<Object> list = oldExchange.getIn().getBody(java.util.ArrayList.class);
                        list.add(newBody);
                        return oldExchange;
                    }
                })
                .to("direct:sgRoute1")
                .to("direct:sgRoute2")
                .to("direct:sgRoute3")
            .end() // End of multicast
            
            .log("After scatter-gather, aggregated body (List of Maps): ${body}")
            
            // Call the bean to perform the "payload..payload" like transformation.
            // The current bean method `aggregateFromListOfMaps` takes List<Map<String, Object>>
            // and returns List<Object>. This matches the expectation.
            .bean(scatterGatherAggregationBean, "aggregateFromListOfMaps")
            .log("After ScatterGatherAggregationBean: ${body}")
            
            // Marshal the final result to JSON
            .marshal().json(JsonLibrary.Jackson)
            .log("Final JSON response: ${body}")
            .setHeader("Content-Type", constant("application/json"));

        // Individual routes for the scatter-gather legs
        from("direct:sgRoute1")
            .routeId("scatter-gather-route1")
            .setBody(constant(Map.of(
                "payload01Name", "Manju",
                "payload01ID", 1,
                "payload01Location", "INDIA",
                "payload01Salary", 2000
            )))
            .log("Scatter-Gather Route 1 Payload: ${body}");

        from("direct:sgRoute2")
            .routeId("scatter-gather-route2")
            .setBody(constant(Map.of(
                "payload02Name", "Peter",
                "payload02ID", 2,
                "payload02Location", "UK",
                "payload02Salary", 1000
            )))
            .log("Scatter-Gather Route 2 Payload: ${body}");

        from("direct:sgRoute3")
            .routeId("scatter-gather-route3")
            .setBody(constant(Map.of(
                "payload03Name", "Rahul",
                "payload03ID", 3,
                "payload03Location", "USA",
                "payload03Salary", 3000
            )))
            .log("Scatter-Gather Route 3 Payload: ${body}");
    }
}
