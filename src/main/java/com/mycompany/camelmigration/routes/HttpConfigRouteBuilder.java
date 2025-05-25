package com.mycompany.camelmigration.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HttpConfigRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Main flow corresponding to asycnAndChoiceFlow triggered by HTTP
        from("platform-http:/asyncchoice?httpMethodRestrict=GET,POST")
            .routeId("http-asyncchoice-listener")
            .log("Received request on /asyncchoice (thread: ${threadName})")
            // Implement the initial <set-payload> from asycnAndChoiceFlow
            // Mule XML: <set-payload value='#[{ "ip": "000.00.00.01", ...}]' />
            .process(exchange -> {
                Map<String, Object> initialPayload = new HashMap<>();
                initialPayload.put("ip", "000.00.00.01");
                initialPayload.put("hostname", "mspapi-ctwbcp05-finance.nyc.gov");
                initialPayload.put("city", "New York City");
                initialPayload.put("region", "New York");
                initialPayload.put("country", "US");
                initialPayload.put("loc", "40.7143,-74.0060");
                initialPayload.put("org", "AS22252 The City of New York");
                initialPayload.put("postal", "10004");
                initialPayload.put("timezone", "America/New_York");
                initialPayload.put("readme", "https://ipinfo.io/missingauth");
                exchange.getIn().setBody(initialPayload);
            })
            .log("Initial payload set (thread: ${threadName}): ${body}")
            // Implement the <async> block by sending to a SEDA endpoint
            // This makes the rest of the processing asynchronous from the HTTP response
            .to("seda:processAsyncChoice")
            // Immediately respond to the HTTP client after dispatching to SEDA
            // The original Mule flow implies the async block does not contribute to the final HTTP response directly
            // unless there's a request-reply pattern over JMS or another mechanism.
            // For a simple async hand-off:
            .transform().constant("Request for asyncchoice received and is being processed asynchronously.")
            .marshal().json(JsonLibrary.Jackson);


        // SEDA route for asynchronous processing (contents of the <async> block)
        from("seda:processAsyncChoice")
            .routeId("seda-process-async-choice")
            .log("Async processing started for choice logic (thread: ${threadName}): ${body}")
            // Call IpinfoHttpPoc_Subflow
            .to("direct:ipInfoPocSubflow")
            .log("After ipInfoPocSubflow (thread: ${threadName}): ${body}")
            // Parse the JSON response from ipinfo.io into a Map
            .unmarshal().json(JsonLibrary.Jackson, Map.class)
            .log("Parsed ipInfo response (thread: ${threadName}): ${body}")
            
            // Implement the Choice Router
            .choice()
                .when(simple("${body[ip]} in '161.185.160.99,161.185.160.98'")) // Checks if the 'ip' field in the Map is one of the specified values
                    .log("Choice 'when' block: IP matched (thread: ${threadName})")
                    .to("direct:abstractFlowForAsync")
                    // The body is now the result of "direct:abstractFlowForAsync" (a Map)
                    // Marshal it to JSON for the final output of this path
                    .marshal().json(JsonLibrary.Jackson)
                    .log("After 'when' block, marshalled payload (thread: ${threadName}): ${body}")
                .otherwise()
                    .log(LoggingLevel.INFO, "++++ ROUTER OTHERWISE (thread: ${threadName})")
                    // Set payload to the static JSON string "default value"
                    .transform().constant("\"default value\"") // Ensure it's a valid JSON string
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .log("Choice 'otherwise' block, payload set to default value (thread: ${threadName}): ${body}")
            .endChoice() // End of choice block
            .log("Async processing completed (thread: ${threadName}): ${body}");

        // NOTE for ScatterGatherModule.xml:
        // If scatterGatherModuleFlow were implemented here, its final transformation:
        // <ee:transform doc:name="Convert to JSON and send HTTP Response">
        //   <ee:message><ee:set-payload><![CDATA[%dw 2.0 output application/json --- payload..payload]]></ee:set-payload></ee:message>
        // </ee:transform>
        // Would be translated using the ScatterGatherAggregationBean. For example:
        // from("direct:someScatterGatherAggPoint")
        //     .bean("scatterGatherAggregationBean", "aggregateFromListOfMaps") // Assuming input is List<Map>
        //     .marshal().json(JsonLibrary.Jackson)
        //     .log("Aggregated and transformed scatter-gather result: ${body}");
        // The actual scatter-gather EIP would precede this.

        // Implementation of IpinfoHttpPoc_Subflow
        from("direct:ipInfoPocSubflow")
            .routeId("subflow-ipinfo-http-poc")
            .log("IpinfoHttpPoc_Subflow started (thread: ${threadName})")
            .to("https://ipinfo.io/161.185.160.99/geo?bridgeEndpoint=true&throwExceptionOnFailure=false")
            .log(LoggingLevel.DEBUG, "Raw response from ipinfo.io: ${body}")
            .log("IpinfoHttpPoc_Subflow completed (thread: ${threadName}). Response body: ${body}");

        // Implementation of AbstractFlowForAsync Subflow
        from("direct:abstractFlowForAsync")
            .routeId("subflow-abstract-flow-async")
            .log("AbstractFlowForAsync started (thread: ${threadName})")
            // Implement the <set-payload value='#[{"abstractName":"Test", "abstractCity":"TestCity", "abstractCountry":"TestCountry"}]' />
            .process(exchange -> {
                Map<String, String> abstractPayload = new HashMap<>();
                abstractPayload.put("abstractName", "Test");
                abstractPayload.put("abstractCity", "TestCity");
                abstractPayload.put("abstractCountry", "TestCountry");
                exchange.getIn().setBody(abstractPayload);
            })
            .log("AbstractFlowForAsync completed (thread: ${threadName}). Payload: ${body}");
    }
}
