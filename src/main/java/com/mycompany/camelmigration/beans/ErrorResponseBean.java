package com.mycompany.camelmigration.beans;

import org.apache.camel.Body;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("errorResponseBean") // Explicit bean name for clarity
public class ErrorResponseBean {

    public Map<String, String> createDbBadSqlSyntaxError() {
        return Map.of("errorType", "DB:BAD_SQL_SYNTAX", "errorMessage", "Bad Syntax");
    }

    public Map<String, String> createDbConnectivityError() {
        return Map.of("errorType", "DB:CONNECTIVITY", "errorMessage", "Database Connectivity Error");
    }
    
    public Map<String, String> createGenericDbError() {
        return Map.of("errorType", "DB:GENERIC_SQL_ERROR", "errorMessage", "A database error occurred.");
    }

    public Map<String, String> createHttpBadGatewayError() {
        return Map.of("errorType", "HTTP:BAD_GATEWAY", "errorMessage", "Bad Gateway encountered");
    }

    public Map<String, String> createHttpBadRequestError() {
        return Map.of("errorType", "HTTP:BAD_REQUEST", "errorMessage", "Bad Request");
    }

    public Map<String, String> createHttpClientSecurityError() {
        return Map.of("errorType", "HTTP:CLIENT_SECURITY", "errorMessage", "Client security error (Unauthorized or Forbidden)");
    }

    // Method to receive statusCode, e.g., from a header or property
    public Map<String, String> createGenericHttpError(@ExchangeProperty("CamelExceptionStatusCode") int statusCode) {
        Map<String, String> error = new HashMap<>();
        error.put("errorType", "HTTP:OTHER_ERROR");
        error.put("errorMessage", "An HTTP error occurred: " + statusCode);
        return error;
    }
    
    // Method to receive the exception message, e.g., using @Body for the exception object
    // Or more simply, pass the message via Simple expression if it's readily available.
    // Here, we assume the message is passed as a parameter via Simple expression.
    public Map<String, String> createGenericError(String exceptionMessage) {
        Map<String, String> error = new HashMap<>();
        error.put("errorType", "GENERIC_ERROR");
        // Sanitize or shorten the message if necessary
        String message = (exceptionMessage != null && !exceptionMessage.isEmpty()) ? exceptionMessage : "An unexpected error occurred.";
        if (message.length() > 255) { // Example sanitization
            message = message.substring(0, 252) + "...";
        }
        error.put("errorMessage", message);
        return error;
    }
}
