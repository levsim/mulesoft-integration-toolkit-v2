# Mulesoft to Apache Camel Component Mapping

This document outlines the Apache Camel equivalents for common Mulesoft components.

---

**1. Mulesoft Component: HTTP Listener**
*   **Apache Camel Component/Equivalent:** `camel-jetty`, `camel-netty-http`, `camel-undertow`, `camel-platform-http-starter` (for Spring Boot)
*   **Camel EIP Name (if applicable):** Message Endpoint (specifically an inbound HTTP endpoint)
*   **Java DSL Implementation Strategy Note:** Use `from("jetty:http://0.0.0.0:8080/yourpath")` or `from("netty-http:http://0.0.0.0:8080/yourpath")`. Configuration of host, port, path, SSL, etc., is done via endpoint URI parameters or component configuration.

---

**2. Mulesoft Component: HTTP Requestor**
*   **Apache Camel Component/Equivalent:** `camel-http` (Apache HTTP Client), `camel-jetty` (as an HTTP client), `camel-netty-http` (as an HTTP client), `camel-undertow` (as an HTTP client)
*   **Camel EIP Name (if applicable):** Service Activator / Message Translator (if request/response transformation is involved)
*   **Java DSL Implementation Strategy Note:** Use `.to("http://hostname/path?options")` or `.toD("http://hostname/path?options")` for dynamic URIs. For more control, use `enrich` or `pollEnrich` with an HTTP endpoint. Headers are often used to pass/receive HTTP headers.

---

**3. Mulesoft Component: Choice Router**
*   **Apache Camel Component/Equivalent:** Core routing EIPs
*   **Camel EIP Name (if applicable):** Content-Based Router
*   **Java DSL Implementation Strategy Note:** Use `.choice()` followed by one or more `.when(predicate)` and optionally `.otherwise()`. Predicates can be Simple expressions, Header values, Bean methods, etc.
    ```java
    .choice()
        .when(header("type").isEqualTo("A"))
            .to("direct:typeA")
        .when(simple("${body} contains 'Camel'"))
            .to("direct:camelSpecific")
        .otherwise()
            .to("direct:defaultProcessing")
    .end();
    ```

---

**4. Mulesoft Component: Scatter-Gather**
*   **Apache Camel Component/Equivalent:** Core routing EIPs
*   **Camel EIP Name (if applicable):** Scatter-Gather
*   **Java DSL Implementation Strategy Note:** Use the `recipientList` EIP with parallel processing and a custom `AggregationStrategy`. Alternatively, for simpler cases, multiple `.to()` calls within a `multicast` with `parallelProcessing()` and a custom aggregator.
    ```java
    // Using multicast
    .multicast(new MyAggregationStrategy())
        .parallelProcessing()
        .to("direct:route1")
        .to("direct:route2")
        .to("direct:route3")
    .end(); // end of multicast

    // Using recipientList for more dynamic routing
    // .recipientList(header("endpointsHeader")).aggregationStrategy(new MyAggregationStrategy()).parallelProcessing();
    ```

---

**5. Mulesoft Component: Async processing blocks (`<async>`)**
*   **Apache Camel Component/Equivalent:** `camel-seda`, `camel-jms`, `camel-kafka`, Thread Pools, `Wire Tap` EIP
*   **Camel EIP Name (if applicable):** Pipes and Filters (often with asynchronous stages), Event Message
*   **Java DSL Implementation Strategy Note:**
    *   For fire-and-forget: Use `.to("seda:someQueue")` or `.wireTap("direct:anotherRoute").newExchangeBody(body())`.
    *   For offloading processing to another thread: Use `.threads()` EIP (e.g., `.threads().poolSize(5)`) before a processing block.
    *   For request-reply with a separate thread pool: Can be more complex, potentially involving `SEDA` with custom correlation or specific JMS/Kafka request-reply patterns.
    ```java
    // Fire and forget
    .to("seda:backgroundTask");

    // Offload a block of processing
    .threads(5, 10, "myAsyncPool")
        .to("direct:heavyProcessing")
    .end();
    ```

---

**6. Mulesoft Component: Flow-refs (sub-flows)**
*   **Apache Camel Component/Equivalent:** `direct` component, `direct-vm` (for cross-context), or calling bean methods.
*   **Camel EIP Name (if applicable):** Pipes and Filters
*   **Java DSL Implementation Strategy Note:** Define reusable route segments starting with `from("direct:routeName")`. Invoke them using `.to("direct:routeName")`.
    ```java
    // Main route
    from("direct:mainFlow")
        .to("direct:subFlow1")
        .to("direct:subFlow2");

    // Sub-flow 1
    from("direct:subFlow1")
        .log("In subFlow1");
        // ... processing ...

    // Sub-flow 2
    from("direct:subFlow2")
        .log("In subFlow2");
        // ... processing ...
    ```

---

**7. Mulesoft Component: Set Payload / Set Variable (and similar message modification components)**
*   **Apache Camel Component/Equivalent:** `transform()`, `setProperty()`, `setHeader()`, `process()`, `bean()`
*   **Camel EIP Name (if applicable):** Message Translator, Content Enricher
*   **Java DSL Implementation Strategy Note:**
    *   Set message body (payload): `.transform().simple("New body")`, `.transform().constant("New Body")`, `.setBody(simple("${header.myHeader}"))`, or `.process(exchange -> exchange.getIn().setBody(...))`
    *   Set message headers (variables/attributes in Mule): `.setHeader("headerName", constant("value"))`
    *   Set exchange properties (variables in Mule, Camel's equivalent of flowVars): `.setProperty("propertyName", constant("value"))`
    ```java
    .setBody(simple("Transformed Payload: ${body}"))
    .setHeader("myHeader", constant("myHeaderValue"))
    .setProperty("myProperty", simple("${date:now:yyyyMMdd}"));
    ```

---

**8. Mulesoft Component: Logger**
*   **Apache Camel Component/Equivalent:** `log` EIP, `camel-log` component (uses SLF4J by default)
*   **Camel EIP Name (if applicable):** Message History (implicitly, as logs can show route progress)
*   **Java DSL Implementation Strategy Note:** Use `.log("Logging message: ${body}")`. Can specify logging level and logger name.
    ```java
    .log(LoggingLevel.INFO, "MyLogger", "Processing message with ID ${id} and body ${body}");
    ```

---

**9. Mulesoft Component: Configuration Properties loading**
*   **Apache Camel Component/Equivalent:** Properties Component (`camel-properties`), Spring Property Placeholders, MicroProfile Config.
*   **Camel EIP Name (if applicable):** N/A (this is a framework feature)
*   **Java DSL Implementation Strategy Note:** Use `{{propertyName}}` syntax in endpoint URIs or Java code. Configure the Properties component, e.g., `getContext().getPropertiesComponent().setLocation("classpath:myconfig.properties");`. In Spring Boot, properties from `application.properties` are automatically available.
    ```java
    // In a route definition
    from("file:{{input.directory}}?fileName={{input.filename}}")
        .to("jms:queue:{{output.queue}}");

    // Configuration (e.g., in main or setup class)
    // context.getPropertiesComponent().setLocation("classpath:application.properties");
    ```

---

**10. Mulesoft Component: Global Error Handlers (`on-error-propagate`, `on-error-continue`)**
*   **Apache Camel Component/Equivalent:** `errorHandler()`, `onException()`
*   **Camel EIP Name (if applicable):** Dead Letter Channel, Exception Clause
*   **Java DSL Implementation Strategy Note:**
    *   **`on-error-propagate` equivalent:** Define an `onException(Exception.class).handled(false)...` (default behavior if not specified) or a specific exception. The error is propagated back to the caller.
    *   **`on-error-continue` equivalent:** Define an `onException(Exception.class).handled(true)...`. The error is "caught" and normal processing (or custom error response) continues from the error handler.
    *   Global error handlers: Defined at the `CamelContext` level or per-route using `errorHandler(deadLetterChannel(...))` or `errorHandler(defaultErrorHandler())`.
    *   Specific exception handling: `onException(MySpecificException.class).maximumRedeliveries(2).redeliveryDelay(1000).to("direct:customErrorHandler");`
    ```java
    // Global (CamelContext level) or per-route
    // errorHandler(deadLetterChannel("jms:queue:deadLetters").useOriginalMessage());

    // Specific exception handling within a route builder class
    onException(IOException.class)
        .handled(true) // Equivalent to on-error-continue
        .maximumRedeliveries(3)
        .log("IO Exception occurred: ${exception.message}")
        .setBody(constant("Error processing file"));

    onException(MyCustomBusinessException.class)
        .handled(false) // Equivalent to on-error-propagate
        .log(LoggingLevel.ERROR, "Business exception: ${exception.message}");
    ```

---

**11. Mulesoft Component: Database Connector (CRUD operations)**
*   **Apache Camel Component/Equivalent:** `camel-sql`, `camel-jdbc`, `camel-jpa`, `camel-mybatis`
*   **Camel EIP Name (if applicable):** Service Activator
*   **Java DSL Implementation Strategy Note:**
    *   `camel-sql`: Use endpoint `sql:SELECT * FROM table WHERE id=:#idHeader[dataSourceRef]` for queries, or `sql-stored:procedureName[dataSourceRef]` for stored procedures. Parameters passed via headers or message body.
    *   `camel-jdbc`: Similar to `camel-sql` but provides more direct JDBC access. Output usually `List<Map<String, Object>>`.
    *   `camel-jpa`: For ORM-based persistence using JPA entities. `to("jpa:com.example.MyEntity")` for persisting, or `from("jpa:...")` for consuming entities.
    ```java
    // Using camel-sql for a select query
    from("direct:selectData")
        .setHeader("myId", simple("${body.id}")) // Assuming body has an id field
        .to("sql:SELECT * FROM my_table WHERE id = :#myId?dataSource=#yourDataSource")
        .log("Query result: ${body}");

    // Using camel-sql for an insert
    from("direct:insertData")
        .to("sql:INSERT INTO my_table (name, value) VALUES (:#${body.name}, :#${body.value})?dataSource=#yourDataSource");

    // Using camel-jpa to persist an entity
    // .to("jpa:com.yourpackage.YourEntity");
    ```

---

**12. Mulesoft Component: File Connector (read/write operations)**
*   **Apache Camel Component/Equivalent:** `camel-file`
*   **Camel EIP Name (if applicable):** Message Endpoint (for both consuming and producing files)
*   **Java DSL Implementation Strategy Note:**
    *   Read files (consumer): `from("file:/path/to/input/directory?delete=true&move=.done")`
    *   Write files (producer): `.to("file:/path/to/output/directory?fileName=${header.myFileName}.txt")`
    *   Many options for filtering, sorting, idempotency, locking, etc.
    ```java
    // Read files
    from("file:data/input?noop=true&idempotent=true&move=.processed")
        .log("Processing file: ${header.CamelFileName}");

    // Write files
    from("direct:writeFile")
        .setHeader("CamelFileName", simple("output-${date:now:yyyyMMddHHmmssSSS}.txt"))
        .setBody(simple("This is the content of the file."))
        .to("file:data/output");
    ```

---

**13. Mulesoft Component: ObjectStore Connector (store/retrieve operations)**
*   **Apache Camel Component/Equivalent:** `camel-cache` (using EhCache, Caffeine, etc.), `camel-hazelcast` (for distributed cache/map), `camel-leveldb` (for persistent key-value store), or custom solutions using a bean and a backing store (e.g., database, Redis via `camel-spring-redis`).
*   **Camel EIP Name (if applicable):** Claim Check (if used for temporary offloading of message parts), Idempotent Consumer (if used for de-duplication)
*   **Java DSL Implementation Strategy Note:**
    *   `camel-cache`: `to("cache://myCacheName?operation=add&key=myKey")`, `to("cache://myCacheName?operation=get&key=myKey")`.
    *   `camel-leveldb`: For a simple persistent key-value store.
    *   Custom bean: A common approach for more complex logic or specific backing stores not directly supported.
    ```java
    // Store to cache (e.g., EhCache via camel-cache)
    .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_ADD))
    .setHeader(CacheConstants.CACHE_KEY, simple("objectKey-${id}"))
    .to("cache://myObjectStore");

    // Retrieve from cache
    .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_GET))
    .setHeader(CacheConstants.CACHE_KEY, simple("objectKey-${id}"))
    .to("cache://myObjectStore")
    .choice()
        .when(header(CacheConstants.CACHE_ELEMENT_WAS_FOUND).isNotNull())
            .log("Retrieved from ObjectStore: ${body}")
        .otherwise()
            .log("Object not found in ObjectStore");
    ```

---

**14. Mulesoft Component: JMS Connector (using ActiveMQ)**
*   **Apache Camel Component/Equivalent:** `camel-jms` (generic JMS), `camel-activemq` (optimized for ActiveMQ)
*   **Camel EIP Name (if applicable):** Messaging (Point-to-Point, Publish-Subscribe)
*   **Java DSL Implementation Strategy Note:**
    *   Requires JMS ConnectionFactory configuration (often via Spring or JNDI).
    *   Send to queue/topic: `.to("jms:queue:myQueueName")` or `.to("activemq:topic:myTopicName")`
    *   Consume from queue/topic: `from("jms:queue:myQueueName")` or `from("activemq:topic:myTopicName?durableSubscriptionName=mySub")`
    ```java
    // Assuming ActiveMQComponent is configured (e.g., via Spring Boot auto-configuration or manually)

    // Send to an ActiveMQ queue
    from("direct:sendToJms")
        .to("activemq:queue:ORDERS.IN");

    // Consume from an ActiveMQ queue
    from("activemq:queue:ORDERS.IN")
        .log("Received JMS message: ${body}");

    // Send to an ActiveMQ topic
    from("direct:publishToTopic")
        .to("activemq:topic:PRICE.UPDATES");

    // Consume from an ActiveMQ topic (durable subscriber)
    // from("activemq:topic:PRICE.UPDATES?clientId=myClient&durableSubscriptionName=myAppSubscriber");
    ```

---
