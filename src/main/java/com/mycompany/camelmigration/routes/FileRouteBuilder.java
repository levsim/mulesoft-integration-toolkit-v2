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


        // Implementation of UntilSuccessful_WriteFileFlow
        from("platform-http:/unitsuccesswritefile?httpMethodRestrict=GET")
            .routeId("until-successful-write-file")
            // Configure route-specific error handling for redelivery (mimicking until-successful)
            // This will apply to all subsequent operations in this route.
            .errorHandler(defaultErrorHandler()
                .maximumRedeliveries(5) // Mule's maxRetries="5" (1 initial + 5 retries = 6 total attempts)
                .redeliveryDelay(6000)  // Mule's millisBetweenRetries="6000"
                .retryAttemptedLogLevel(LoggingLevel.WARN) // Log retries at WARN level
                .log("Attempting redelivery for UntilSuccessful_WriteFileFlow due to: ${exception.message}"))
            
            .log(LoggingLevel.INFO, "UntilSuccessful_WriteFileFlow started.")

            // 1. File Write Operation
            // Content from Mule: {"ip": "161.185.160.98", ...}
            // Simplified for this example to avoid very long string.
            .setBody(constant("{\"ip\": \"161.185.160.98\", \"hostname\": \"mspapi-ctwbcp05-fdny.nyc.gov\", \"city\": \"New York City\"}"))
            .log(LoggingLevel.INFO, "Writing to Abstract.txt: ${bodyAs(String)}")
            // autoCreate=false mimics createParentDirectories="false".
            // The directory {{file.base.path}} (e.g., data/untilSuccessfulDemo) must exist.
            .toD("file:{{file.base.path}}?fileName=Abstract.txt&autoCreate=false") 
            .log(LoggingLevel.INFO, "Successfully wrote to Abstract.txt")

            // 2. File Rename Operation
            // Camel's file component can rename (move) a file using the moveExisting option on the producer.
            // We need to ensure the target file doesn't exist or handle it.
            // A simple way is to use a processor, or for simple moves, `moveExisting` is fine.
            // This will move {{file.base.path}}/Abstract.txt to {{file.base.path}}/NewAbstract.txt
            .log(LoggingLevel.INFO, "Attempting to rename Abstract.txt to NewAbstract.txt")
            // The 'toD' endpoint for rename should specify the target file name.
            // The 'moveExisting' option points to the source file to be moved.
            // It's a bit counter-intuitive: you "produce" to the new file, telling it to take content from the old one.
            .toD("file:{{file.base.path}}?fileName=NewAbstract.txt&moveExisting=${properties:file.base.path}/Abstract.txt")
            .log(LoggingLevel.INFO, "Successfully renamed Abstract.txt to NewAbstract.txt")

            // 3. File Read Operation
            // Using pollEnrich to read the file. Timeout if file not found quickly.
            .pollEnrich("file:{{file.base.path}}?fileName=NewAbstract.txt&noop=true&timeout=1000", 1000) // Added timeout to pollEnrich itself
            .log(LoggingLevel.INFO, "Content of NewAbstract.txt: ${bodyAs(String)}")

            // 4. File List Operation (Simplified)
            // The original Mule <file:list> changes the payload to a list of files.
            // Here, we simplify by just logging the path.
            .log(LoggingLevel.INFO, "File listing would be performed on path: {{file.list.path}}")
            // Optionally, to more closely mimic, set a placeholder body:
            .setBody(simple("File listing for {{file.list.path}} not fully implemented, original payload was: ${bodyAs(String)}"))


            // 5. Final Logger (logs the result of the simplified file list operation)
            .log(LoggingLevel.INFO, "Final payload after until-successful block: ${bodyAs(String)}")

            // Set success response for HTTP client
            .transform().constant("{\"status\": \"UntilSuccessful_WriteFileFlow completed successfully.\"}")
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"));
            
        // NOTE: For the UntilSuccessful_WriteFileFlow to work as intended, especially the retry mechanism
        // when autoCreate=false for the initial write:
        // 1. The base directory (e.g., 'data/untilSuccessfulDemo' from {{file.base.path}})
        //    should NOT exist initially if you want to test the retry on file write failure.
        //    Or, it MUST exist if you want the write to succeed on the first try.
        // 2. The directory for file listing '{{file.list.path}}' (e.g. 'data/listFilesFromHere')
        //    should be created manually if any actual listing logic were to be implemented.
    }
}
