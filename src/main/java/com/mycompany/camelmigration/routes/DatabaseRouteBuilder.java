package com.mycompany.camelmigration.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class DatabaseRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Implementation of implementSCDBFlow_Select from database/implementSCDB.xml
        from("platform-http:/select?httpMethodRestrict=GET")
            .routeId("select-db-http")
            .log(LoggingLevel.INFO, "Received HTTP GET request on /select with query params: ${headers}")

            // Extract 'city' query parameter. platform-http puts query params in headers.
            // Example: /select?city=London -> header.city = "London"
            .choice()
                .when(header("city").isNull())
                    .log(LoggingLevel.WARN, "City query parameter is missing.")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400)) // Bad Request
                    .setBody(constant("{\"error\": \"Query parameter 'city' is required.\"}"))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .otherwise()
                    .log(LoggingLevel.INFO, "Processing request for city: ${header.city}")
                    // Set header for named parameter
                    .setHeader("cityParam", header("city"))
                    // Set parameterized SQL query in the body
                    .setBody(constant("SELECT * FROM traditions WHERE City = :#cityParam"))
                    .log(LoggingLevel.INFO, "Executing Parameterized SQL Query: ${body} with headers ${headers}")

                    // Execute the query using the sql component.
                    // The SQL component will use the message body as the query and headers as parameters.
                    .to("sql:?dataSource=#dataSource&useMessageBodyForSql=true")

                    .log(LoggingLevel.INFO, "Database SELECT Result (raw): ${body}")

                    // Marshal the result (List<Map<String, Object>>) to JSON
                    .marshal().json(JsonLibrary.Jackson)

                    .log(LoggingLevel.INFO, "Database SELECT Result (JSON): ${body}")
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .endChoice(); // End of choice for city parameter check


        // Implementation of implementSCDBFlow_Insert
        from("platform-http:/insertdb?httpMethodRestrict=POST")
            .routeId("insert-db-http")
            .log(LoggingLevel.INFO, "Received HTTP POST request on /insertdb")
            .unmarshal().json(JsonLibrary.Jackson, Map.class) // Unmarshal JSON body to Map
            .log(LoggingLevel.DEBUG, "Parsed Insert Payload: ${body}")
            // Set headers from the parsed map for named parameters
            .setHeader("insertId", simple("${body[id]}"))
            .setHeader("insertCity", simple("${body[city]}"))
            .setHeader("insertTradition", simple("${body[tradition]}"))
            // Set parameterized SQL query in the body
            .setBody(constant("INSERT INTO traditions (ID, City, Tradition) VALUES (:#insertId, :#insertCity, :#insertTradition)"))
            .log(LoggingLevel.INFO, "Executing Parameterized SQL Query: ${body} with headers ${headers}")
            .to("sql:?dataSource=#dataSource&useMessageBodyForSql=true")
            .log(LoggingLevel.INFO, "Database INSERT Result (update count): ${body}")
            .transform().constant("{\"status\": \"Inserted OK\"}")
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"));

        // Implementation of implementSCDBFlow_Update
        from("platform-http:/updatedb?httpMethodRestrict=PUT")
            .routeId("update-db-http")
            .log(LoggingLevel.INFO, "Received HTTP PUT request on /updatedb")
            .unmarshal().json(JsonLibrary.Jackson, Map.class) // Unmarshal JSON body to Map
            .log(LoggingLevel.DEBUG, "Parsed Update Payload: ${body}")
            // Set headers from the parsed map for named parameters
            .setHeader("updateTradition", simple("${body[tradition]}"))
            .setHeader("updateId", simple("${body[id]}"))
            // Set parameterized SQL query in the body
            .setBody(constant("UPDATE traditions SET Tradition = :#updateTradition WHERE ID = :#updateId"))
            .log(LoggingLevel.INFO, "Executing Parameterized SQL Query: ${body} with headers ${headers}")
            .to("sql:?dataSource=#dataSource&useMessageBodyForSql=true")
            .log(LoggingLevel.INFO, "Database UPDATE Result (update count): ${body}")
            .transform().constant("{\"status\": \"Updated OK\"}")
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"));

        // Implementation of implementSCDBFlow_Delete
        from("platform-http:/deletedb?httpMethodRestrict=DELETE")
            .routeId("delete-db-http")
            .log(LoggingLevel.INFO, "Received HTTP DELETE request on /deletedb with query params: ${headers}")
            .choice()
                .when(header("id").isNull())
                    .log(LoggingLevel.WARN, "ID query parameter is missing for DELETE.")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                    .setBody(constant("{\"error\": \"Query parameter 'id' is required for delete.\"}"))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .otherwise()
                    .log(LoggingLevel.INFO, "Processing DELETE request for ID: ${header.id}")
                    // Set header for named parameter
                    .setHeader("deleteId", header("id"))
                    // Set parameterized SQL query in the body
                    .setBody(constant("DELETE FROM traditions WHERE ID = :#deleteId"))
                    .log(LoggingLevel.INFO, "Executing Parameterized SQL Query: ${body} with headers ${headers}")
                    .to("sql:?dataSource=#dataSource&useMessageBodyForSql=true")
                    .log(LoggingLevel.INFO, "Database DELETE Result (update count): ${body}")
                    .transform().constant("{\"status\": \"Deleted OK\"}")
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .endChoice();
            
        // Note: A javax.sql.DataSource bean named "dataSource" must be configured
        // in a Spring Boot configuration class (e.g., in com.mycompany.camelmigration.config)
        // for the SQL component to work. This will be addressed in a subsequent step.
        // Example for H2 in-memory DB (to be added to a @Configuration class):
        /*
        @Bean
        public DataSource dataSource() {
            return DataSourceBuilder.create()
                .url("jdbc:h2:mem:traditionsdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                .username("sa")
                .password("")
                .driverClassName("org.h2.Driver")
                .build();
        }
        
        // Additionally, an initialization script (e.g., schema.sql or data.sql in src/main/resources)
        // would be needed to create the 'traditions' table and populate it for testing.
        // Example schema.sql:
        // CREATE TABLE traditions (id INT PRIMARY KEY, City VARCHAR(255), Details VARCHAR(255));
        // INSERT INTO traditions (id, City, Details) VALUES (1, 'London', 'Tea time, Big Ben');
        // INSERT INTO traditions (id, City, Details) VALUES (2, 'Paris', 'Eiffel Tower, Croissants');
        */
    }
}
