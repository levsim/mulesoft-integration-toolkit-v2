package com.mycompany.camelmigration.beans;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseBeanTest {

    private ErrorResponseBean errorResponseBean;

    @BeforeEach
    void setUp() {
        errorResponseBean = new ErrorResponseBean();
    }

    @Test
    @DisplayName("Test createDbBadSqlSyntaxError")
    void testCreateDbBadSqlSyntaxError() {
        Map<String, String> error = errorResponseBean.createDbBadSqlSyntaxError();
        assertEquals("DB:BAD_SQL_SYNTAX", error.get("errorType"));
        assertEquals("Bad Syntax", error.get("errorMessage"));
        assertEquals(2, error.size());
    }

    @Test
    @DisplayName("Test createDbConnectivityError")
    void testCreateDbConnectivityError() {
        Map<String, String> error = errorResponseBean.createDbConnectivityError();
        assertEquals("DB:CONNECTIVITY", error.get("errorType"));
        assertEquals("Database Connectivity Error", error.get("errorMessage"));
        assertEquals(2, error.size());
    }
    
    @Test
    @DisplayName("Test createGenericDbError")
    void testCreateGenericDbError() {
        Map<String, String> error = errorResponseBean.createGenericDbError();
        assertEquals("DB:GENERIC_SQL_ERROR", error.get("errorType"));
        assertEquals("A database error occurred.", error.get("errorMessage"));
        assertEquals(2, error.size());
    }

    @Test
    @DisplayName("Test createHttpBadGatewayError")
    void testCreateHttpBadGatewayError() {
        Map<String, String> error = errorResponseBean.createHttpBadGatewayError();
        assertEquals("HTTP:BAD_GATEWAY", error.get("errorType"));
        assertEquals("Bad Gateway encountered", error.get("errorMessage"));
        assertEquals(2, error.size());
    }

    @Test
    @DisplayName("Test createHttpBadRequestError")
    void testCreateHttpBadRequestError() {
        Map<String, String> error = errorResponseBean.createHttpBadRequestError();
        assertEquals("HTTP:BAD_REQUEST", error.get("errorType"));
        assertEquals("Bad Request", error.get("errorMessage"));
        assertEquals(2, error.size());
    }

    @Test
    @DisplayName("Test createHttpClientSecurityError")
    void testCreateHttpClientSecurityError() {
        Map<String, String> error = errorResponseBean.createHttpClientSecurityError();
        assertEquals("HTTP:CLIENT_SECURITY", error.get("errorType"));
        assertEquals("Client security error (Unauthorized or Forbidden)", error.get("errorMessage"));
        assertEquals(2, error.size());
    }

    @Test
    @DisplayName("Test createGenericHttpError with status code")
    void testCreateGenericHttpError() {
        int statusCode = 503;
        Map<String, String> error = errorResponseBean.createGenericHttpError(statusCode);
        assertEquals("HTTP:OTHER_ERROR", error.get("errorType"));
        assertEquals("An HTTP error occurred: " + statusCode, error.get("errorMessage"));
        assertEquals(2, error.size());
    }
    
    @Test
    @DisplayName("Test createGenericError with a sample message")
    void testCreateGenericErrorWithMessage() {
        String message = "A test exception occurred";
        Map<String, String> error = errorResponseBean.createGenericError(message);
        assertEquals("GENERIC_ERROR", error.get("errorType"));
        assertEquals(message, error.get("errorMessage"));
        assertEquals(2, error.size());
    }

    @Test
    @DisplayName("Test createGenericError with a null message")
    void testCreateGenericErrorWithNullMessage() {
        Map<String, String> error = errorResponseBean.createGenericError(null);
        assertEquals("GENERIC_ERROR", error.get("errorType"));
        assertEquals("An unexpected error occurred.", error.get("errorMessage"));
        assertEquals(2, error.size());
    }

    @Test
    @DisplayName("Test createGenericError with an empty message")
    void testCreateGenericErrorWithEmptyMessage() {
        Map<String, String> error = errorResponseBean.createGenericError("");
        assertEquals("GENERIC_ERROR", error.get("errorType"));
        assertEquals("An unexpected error occurred.", error.get("errorMessage"));
        assertEquals(2, error.size());
    }
    
    @Test
    @DisplayName("Test createGenericError with a long message")
    void testCreateGenericErrorWithLongMessage() {
        String longMessage = "This is a very long error message that exceeds the typical display limit of two hundred and fifty-five characters. It is designed to test the sanitization logic within the createGenericError method to ensure that extremely long messages are truncated appropriately to prevent issues with logging or display in various systems that might have restrictions on field length. The end of this message should be cut off and replaced with ellipses.";
        Map<String, String> error = errorResponseBean.createGenericError(longMessage);
        assertEquals("GENERIC_ERROR", error.get("errorType"));
        assertTrue(error.get("errorMessage").endsWith("..."), "Error message should be truncated with '...'");
        assertTrue(error.get("errorMessage").length() <= 255, "Error message should not exceed 255 characters");
        assertEquals(2, error.size());
    }
}
