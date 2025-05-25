package com.mycompany.camelmigration.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class FileRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // File Write Flow
        from("platform-http:/writefile?httpMethodRestrict=POST")
            .routeId("write-file-http")
            .log(LoggingLevel.INFO, "Received content to write to file: ${bodyAs(String)}")
            // Using .toD to allow dynamic parts in the URI, including properties and Simple expressions
            .toD("file:{{file.output.directory}}?fileName=output-${date:now:yyyyMMddHHmmssSSS}.txt&autoCreate=true")
            .log(LoggingLevel.INFO, "Successfully wrote file to {{file.output.directory}} with name output-${date:now:yyyyMMddHHmmssSSS}.txt")
            .transform().constant("{\"status\": \"File written successfully\"}")
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"));

        // File Read Flow
        // Consumes files from the input directory, moves them to a processed directory,
        // and logs their content. delay=5000ms (5 seconds poll interval).
        // initialDelay=1000ms to start polling after 1 second.
        // autoCreate=true on the consumer side ensures the input directory is created if it doesn't exist.
        from("file:{{file.input.directory}}?move={{file.processed.directory}}&delay=5000&initialDelay=1000&autoCreate=true")
            .routeId("file-consumer")
            .log(LoggingLevel.INFO, "Read file: ${header.CamelFileName} from {{file.input.directory}}")
            .log(LoggingLevel.DEBUG, "File content: ${bodyAs(String)}") // Log content at DEBUG or as needed
            // Optional: Simple processing (e.g., convert to uppercase)
            .transform().simple("${bodyAs(String).toUpperCase()}")
            .log(LoggingLevel.INFO, "Processed file: ${header.CamelFileName}, Uppercase content: ${bodyAs(String)}")
            // Further processing can be added here, e.g., sending to another route, a bean, etc.
            .to("log:fileProcessingOutput?showBody=true&showHeaders=true"); // Example of sending to another log endpoint
    }
}
