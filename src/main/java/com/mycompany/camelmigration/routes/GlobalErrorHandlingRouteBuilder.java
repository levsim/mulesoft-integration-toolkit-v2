package com.mycompany.camelmigration.routes;

import com.mycompany.camelmigration.beans.ErrorResponseBean;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class GlobalErrorHandlingRouteBuilder extends RouteBuilder {

    @Autowired
    private ErrorResponseBean errorResponseBean;

    @Override
    public void configure() throws Exception {

        // Default Dead Letter Channel (DLC)
        // Logs errors and message details if no specific onException handles the error, or if an onException rethrows.
        errorHandler(deadLetterChannel("log:deadLetter?level=ERROR&showAll=true&multiline=true")
            .maximumRedeliveries(3) // Example: try up to 3 redeliveries
            .redeliveryDelay(1000)  // Example: 1 second delay between redeliveries
            .useOriginalMessage()   // Log the original message that caused the error
            .logRetryAttempted(true)
            .retriesExhaustedLogLevel(LoggingLevel.ERROR)
            .deadLetterHandleNewException(false)); // Let original exception propagate if all redeliveries fail and no specific onException handles it.

        // Equivalent to Mule's 'handleAnyMathematicalError'
        // Handling ArithmeticException as a common example for math errors.
        // MATH:STRING_EXCEPTION from Mule might be broader; consider a custom exception or more generic Exception.class if needed.
        onException(ArithmeticException.class)
            .routeId("handleAnyMathematicalError")
            .handled(false) // Equivalent to on-error-propagate
            .log(LoggingLevel.ERROR, "GlobalErrorHandler: ArithmeticException caught by handleAnyMathematicalError. Message: ${exception.message}, ExceptionType: ${exception.class.name}, Stacktrace: ${exception.stacktrace}");

        // Equivalent to Mule's 'handleDatabaseErrorOnly'
        onException(SQLException.class)
            .routeId("handleDatabaseErrorOnly")
            .handled(true) // Equivalent to on-error-continue
            .log(LoggingLevel.ERROR, "GlobalErrorHandler: SQLException caught by handleDatabaseErrorOnly. Message: ${exception.message}, SQLState: ${exception.sqlState}, ErrorCode: ${exception.errorCode}, ExceptionType: ${exception.class.name}")
            .choice()
                // Attempt to differentiate based on common SQLState prefixes or message content.
                // This is indicative; actual codes/messages depend on the JDBC driver and database.
                // For DB:BAD_SQL_SYNTAX (e.g., SQLState starting with '42' for many DBs)
                .when(simple("${exception.sqlState} startsWith '42' or ${exception.message} contains 'syntax error'"))
                    .log(LoggingLevel.WARN, "GlobalErrorHandler (handleDatabaseErrorOnly): Recognized as Bad SQL Syntax.")
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .bean(errorResponseBean, "createDbBadSqlSyntaxError")
                // For DB:CONNECTIVITY (e.g., SQLState starting with '08' or message content)
                .when(simple("${exception.sqlState} startsWith '08' or ${exception.message} contains 'connection' or ${exception.message} contains 'connectivity'"))
                    .log(LoggingLevel.WARN, "GlobalErrorHandler (handleDatabaseErrorOnly): Recognized as DB Connectivity issue.")
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .bean(errorResponseBean, "createDbConnectivityError")
                .otherwise()
                    .log(LoggingLevel.WARN, "GlobalErrorHandler (handleDatabaseErrorOnly): Uncategorized SQLException. Providing generic DB error response.")
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .bean(errorResponseBean, "createGenericDbError")
            .endChoice()
            .marshal().json() // Marshal the Map from bean to JSON string
            .log(LoggingLevel.INFO, "GlobalErrorHandler (handleDatabaseErrorOnly): Processed response body: ${body}");

        // Equivalent to Mule's 'handleHTTPErrors'
        onException(HttpOperationFailedException.class)
            .routeId("handleHTTPErrors")
            .handled(true) // Equivalent to on-error-continue
            .log(LoggingLevel.ERROR, "GlobalErrorHandler: HttpOperationFailedException caught by handleHTTPErrors. URI: ${exception.uri}, StatusCode: ${exception.statusCode}, Message: ${exception.message}, ExceptionType: ${exception.class.name}")
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .setProperty("CamelExceptionStatusCode", simple("${exception.statusCode}")) // Store status code for bean access
            .choice()
                .when(simple("${exception.statusCode} == 502")) // HTTP:BAD_GATEWAY
                    .bean(errorResponseBean, "createHttpBadGatewayError")
                .when(simple("${exception.statusCode} == 400")) // HTTP:BAD_REQUEST
                    .bean(errorResponseBean, "createHttpBadRequestError")
                .when(simple("${exception.statusCode} == 401 || ${exception.statusCode} == 403")) // HTTP:CLIENT_SECURITY (Unauthorized/Forbidden)
                    .bean(errorResponseBean, "createHttpClientSecurityError")
                .otherwise()
                    // Pass the status code to the bean method.
                    // The @ExchangeProperty annotation in the bean method will pick this up.
                    .bean(errorResponseBean, "createGenericHttpError")
            .endChoice()
            .marshal().json() // Marshal the Map from bean to JSON string
            .log(LoggingLevel.INFO, "GlobalErrorHandler (handleHTTPErrors): Processed response body: ${body}");
            
        // Fallback for any other unhandled Exception - for robust general error handling
        onException(Exception.class)
            .routeId("handleGenericError")
            .handled(true) // Typically true for a global catch-all to prevent app crash / return standard error
            .log(LoggingLevel.ERROR, "GlobalErrorHandler: Generic Exception caught by handleGenericError. Message: ${exception.message}, ExceptionType: ${exception.class.name}, Stacktrace: ${exception.stacktrace}")
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            // Pass the exception message to the bean method.
            .bean(errorResponseBean, "createGenericError(${exception.message})")
            .marshal().json() // Marshal the Map from bean to JSON string
            .log(LoggingLevel.INFO, "GlobalErrorHandler (handleGenericError): Processed response body: ${body}");

    }
}
