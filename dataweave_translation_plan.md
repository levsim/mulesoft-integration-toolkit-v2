# DataWeave Translation Plan

This document outlines the plan for translating embedded DataWeave scripts from Mulesoft XML configurations to Apache Camel with Java.

---

## Script 1

**1. Source XML File & Component:**
   - File: `globalErrorHandler.xml`
   - Mule Component Context: `Transform: Transform to DB:BAD_SQL_SYNTAX Error JSON` (within `error-handler name="handleDatabaseErrorOnly"`, `on-error-continue type="DB:BAD_SQL_SYNTAX"`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
---
{
	errorType: "DB:BAD_SQL_SYNTAX",
	errorMessage: "Bad Syntax"
}
```

**3. Script's Purpose:**
   - Creates a standard JSON error response structure for database bad SQL syntax errors.

**4. Translation Complexity:**
   - Simple: Direct mapping to a Java POJO or Map.

**5. Proposed Java/Camel Translation Strategy:**
   - Use a Java Bean method to create a `Map<String, String>` or a simple POJO representing the error structure. Then, use `camel-jackson` (or `camel-gson`) to marshal this object into a JSON string and set it as the message body.
   - **Java Bean Suggestion:**
     ```java
     import java.util.HashMap;
     import java.util.Map;

     public class ErrorResponseBeanFactory {
         public static Map<String, String> createDbBadSqlError() {
             Map<String, String> error = new HashMap<>();
             error.put("errorType", "DB:BAD_SQL_SYNTAX");
             error.put("errorMessage", "Bad Syntax");
             return error;
         }
     }
     // Camel DSL usage in an onException block:
     // .bean(ErrorResponseBeanFactory.class, "createDbBadSqlError")
     // .marshal().json(JsonLibrary.Jackson);
     ```

---

## Script 2

**1. Source XML File & Component:**
   - File: `globalErrorHandler.xml`
   - Mule Component Context: `Transform: Transform to DB:CONNECTIVITY Error JSON` (within `error-handler name="handleDatabaseErrorOnly"`, `on-error-continue type="DB:CONNECTIVITY"`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
---
{
	errorType: "DB:CONNECTIVITY",
	errorMessage: "Unable to connect to Database"
}
```

**3. Script's Purpose:**
   - Creates a standard JSON error response for database connectivity errors.

**4. Translation Complexity:**
   - Simple: Direct mapping.

**5. Proposed Java/Camel Translation Strategy:**
   - Similar to Script 1. Use a Java Bean method to create a `Map` or POJO, then marshal to JSON.
   - **Java Bean Suggestion:**
     ```java
     // (Add to ErrorResponseBeanFactory from Script 1)
     // public static Map<String, String> createDbConnectivityError() {
     //     Map<String, String> error = new HashMap<>();
     //     error.put("errorType", "DB:CONNECTIVITY");
     //     error.put("errorMessage", "Unable to connect to Database");
     //     return error;
     // }
     ```

---

## Script 3

**1. Source XML File & Component:**
   - File: `globalErrorHandler.xml`
   - Mule Component Context: `Transform: Transform to HTTP:BAD_GATEWAY Error JSON` (within `error-handler name="handleHTTPErrors"`, `on-error-continue type="HTTP:BAD_GATEWAY"`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
---
{
	errorType: "HTTP:BAD_GATEWAY",
	errorMessage: "Bad Gateway"
}
```

**3. Script's Purpose:**
   - Creates a standard JSON error response for HTTP Bad Gateway errors.

**4. Translation Complexity:**
   - Simple: Direct mapping.

**5. Proposed Java/Camel Translation Strategy:**
   - Similar to Script 1.
   - **Java Bean Suggestion:**
     ```java
     // (Add to ErrorResponseBeanFactory from Script 1)
     // public static Map<String, String> createHttpBadGatewayError() {
     //     Map<String, String> error = new HashMap<>();
     //     error.put("errorType", "HTTP:BAD_GATEWAY");
     //     error.put("errorMessage", "Bad Gateway");
     //     return error;
     // }
     ```

---

## Script 4

**1. Source XML File & Component:**
   - File: `globalErrorHandler.xml`
   - Mule Component Context: `Transform: Transform to HTTP:BAD_REQUEST_CLIENT_SECURITY Error JSON` (within `error-handler name="handleHTTPErrors"`, `on-error-continue type="HTTP:BAD_REQUEST, HTTP:CLIENT_SECURITY"`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
---
{
	errorType: "HTTP:BAD_REQUEST, HTTP:CLIENT_SECURITY",
	errorMessage: "It's a bad Request or client security"
}
```

**3. Script's Purpose:**
   - Creates a standard JSON error response for HTTP Bad Request or Client Security errors.

**4. Translation Complexity:**
   - Simple: Direct mapping.

**5. Proposed Java/Camel Translation Strategy:**
   - Similar to Script 1.
   - **Java Bean Suggestion:**
     ```java
     // (Add to ErrorResponseBeanFactory from Script 1)
     // public static Map<String, String> createHttpBadRequestClientSecurityError() {
     //     Map<String, String> error = new HashMap<>();
     //     error.put("errorType", "HTTP:BAD_REQUEST, HTTP:CLIENT_SECURITY");
     //     error.put("errorMessage", "It's a bad Request or client security");
     //     return error;
     // }
     ```

---

## Script 5

**1. Source XML File & Component:**
   - File: `scatterGatherModule.xml`
   - Mule Component Context: `Transform: Convert to JSON and send HTTP Response` (after `scatter-gather` in `scatterGatherModuleFlow`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
---
//extract only the payload information
payload..payload
```

**3. Script's Purpose:**
   - Aggregates results from a Scatter-Gather. The `payload..payload` expression extracts all values from fields named `payload` within the (potentially nested) structure returned by the Scatter-Gather component.

**4. Translation Complexity:**
   - Moderate: The `..` (descendant selector) is powerful but its direct Java equivalent depends on the actual structure of the aggregated payload. If the structure is consistent (e.g., a list of maps, each containing a 'payload' key), it's manageable.

**5. Proposed Java/Camel Translation Strategy:**
   - Use a custom Java Bean method as a Camel Aggregation Strategy for the Scatter-Gather EIP, or as a processor after the aggregation. This bean would iterate through the aggregated `List<Object>` (where each Object is the result from a route) or `Map` (if a custom strategy produced a map) and extract the desired parts.
   - **Java Bean Suggestion (assuming scatter-gather results in a `List<Map<String, Object>>` where each map has a 'payload' key to be extracted):**
     ```java
     import java.util.ArrayList;
     import java.util.List;
     import java.util.Map;
     import java.util.stream.Collectors;

     public class ScatterGatherHelper {
         @SuppressWarnings("unchecked")
         public List<Object> extractPayloads(List<Object> aggregatedResults) {
             return aggregatedResults.stream()
                 .filter(obj -> obj instanceof Map)
                 .map(obj -> ((Map<String, Object>) obj).get("payload")) // Extracts the value of "payload" key
                 .filter(payload -> payload != null)
                 .collect(Collectors.toList());
         }
     }
     // Camel DSL:
     // .multicast(new MyAggregationStrategy()) // or a default that produces List<Object>
     //    .parallelProcessing()
     //    .to("direct:route1").to("direct:route2")
     // .end() // end of multicast/scatter-gather
     // .bean(ScatterGatherHelper.class, "extractPayloads")
     // .marshal().json(JsonLibrary.Jackson);
     ```
   - *Note:* If the `payload..payload` implies a more complex deep search, the Java logic would need to be more sophisticated, potentially involving recursion or a library for object graph traversal.

---

## Script 6

**1. Source XML File & Component:**
   - File: `asycnAndChoice.xml`
   - Mule Component Context: `Transform: Transform Message in Choice-When` (within `asycnAndChoiceFlow`, `async` block, `choice` router)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
---
payload
```

**3. Script's Purpose:**
   - Outputs the current Mule message payload as a JSON string.

**4. Translation Complexity:**
   - Simple: Direct mapping.

**5. Proposed Java/Camel Translation Strategy:**
   - If the Camel message body (`exchange.getIn().getBody()`) is a POJO or `Map`, use `.marshal().json(JsonLibrary.Jackson)`. If the body is already a JSON string, this step might just ensure the `Content-Type` header is set.

---

## Script 7

**1. Source XML File & Component:**
   - File: `asycnAndChoice.xml`
   - Mule Component Context: `Transform: Transform Message in Choice-Otherwise` (within `asycnAndChoiceFlow`, `async` block, `choice` router)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
---
"default value"
```

**3. Script's Purpose:**
   - Sets a static string "default value" as the JSON payload.

**4. Translation Complexity:**
   - Simple: Direct mapping.

**5. Proposed Java/Camel Translation Strategy:**
   - `.setBody(constant("default value"))` followed by `.marshal().json(JsonLibrary.Jackson)` if a JSON string literal is strictly required. Or simply `.setBody(constant("\"default value\""))` and ensure `Content-Type` is `application/json`.

---

## Script 8

**1. Source XML File & Component:**
   - File: `asycnAndChoice.xml`
   - Mule Component Context: `Transform: Final Payload Transform in Main Flow` (within `asycnAndChoiceFlow`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
---
payload
```

**3. Script's Purpose:**
   - Outputs the current Mule message payload as a JSON string.

**4. Translation Complexity:**
   - Simple: Direct mapping.

**5. Proposed Java/Camel Translation Strategy:**
   - Same as Script 6: `.marshal().json(JsonLibrary.Jackson)`.

---

## Script 9 (from 1operators.xml)

**1. Source XML File & Component:**
   - File: `1operators.xml`
   - Mule Component Context: `Transform: ++--` (Flow: `operators101`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
---
{
	"conNumbers" : [1,2,3] ++ [4,5,6] ++ ["a"],
	"UnconNumbers" : [1,2,3] -- [1],
	"payload1": "hi" ++ " manju kaushik"
}
```

**3. Script's Purpose:**
   - Demonstrates array concatenation (`++`), array subtraction (`--`), and string concatenation.

**4. Translation Complexity:**
   - Moderate: Requires specific Java collection and string operations.

**5. Proposed Java/Camel Translation Strategy:**
   - Java Bean method to perform these operations.
   - **Java Bean Suggestion:**
     ```java
     import java.util.*;
     import java.util.stream.Collectors;
     import java.util.stream.Stream;

     public class DwOperatorsBean1 {
         public Map<String, Object> collectionAndStringOps() {
             Map<String, Object> result = new HashMap<>();
             List<Object> list1 = new ArrayList<>(Arrays.asList(1, 2, 3));
             List<Object> list2 = Arrays.asList(4, 5, 6);
             List<Object> listA = Collections.singletonList("a");
             result.put("conNumbers", Stream.of(list1, list2, listA).flatMap(Collection::stream).collect(Collectors.toList()));
             
             List<Object> baseList = new ArrayList<>(Arrays.asList(1, 2, 3));
             List<Object> subtractList = Collections.singletonList(1);
             baseList.removeAll(subtractList); // Modifies baseList
             result.put("UnconNumbers", baseList);
             
             result.put("payload1", "hi" + " manju kaushik");
             return result;
         }
     }
     // Camel: .bean(DwOperatorsBean1.class, "collectionAndStringOps").marshal().json();
     ```

---

## Script 10 (from 1operators.xml)

**1. Source XML File & Component:**
   - File: `1operators.xml`
   - Mule Component Context: `Transform: Average` (Flow: `operators101`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
---
{
	"avg1": avg([1,1000]),
	"avg2": avg([1,1000,5687])
}
```

**3. Script's Purpose:**
   - Calculates the average of numbers in arrays.

**4. Translation Complexity:**
   - Simple.

**5. Proposed Java/Camel Translation Strategy:**
   - Java Bean method using Java Streams API.
   - **Java Bean Suggestion:**
     ```java
     import java.util.*;
     import java.util.stream.IntStream;

     public class DwOperatorsBean2 {
         public Map<String, Double> calculateAverages() {
             Map<String, Double> result = new HashMap<>();
             result.put("avg1", IntStream.of(1, 1000).average().orElse(Double.NaN));
             result.put("avg2", IntStream.of(1, 1000, 5687).average().orElse(Double.NaN));
             return result;
         }
     }
     // Camel: .bean(DwOperatorsBean2.class, "calculateAverages").marshal().json();
     ```

---

## Script 11 (from 1operators.xml)

**1. Source XML File & Component:**
   - File: `1operators.xml`
   - Mule Component Context: `Transform: distinctBy` (Flow: `operators101`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
var studentDetails = { "name": "manju", "subjects": ["maths", "physics", "science", "maths"], "semester": "1st" }
---
studentDetails.subjects distinctBy $
```

**3. Script's Purpose:**
   - Removes duplicate elements from the `subjects` array within the `studentDetails` object.

**4. Translation Complexity:**
   - Simple to Moderate: Requires getting the input, extracting the list, and applying distinct.

**5. Proposed Java/Camel Translation Strategy:**
   - Java Bean method. Input would be a `Map` (representing `studentDetails`).
   - **Java Bean Suggestion:**
     ```java
     import java.util.*;
     import java.util.stream.Collectors;

     public class DwOperatorsBean3 {
         @SuppressWarnings("unchecked")
         public List<String> getDistinctSubjects(Map<String, Object> studentDetails) {
             List<String> subjects = (List<String>) studentDetails.getOrDefault("subjects", Collections.emptyList());
             return subjects.stream().distinct().collect(Collectors.toList());
         }
     }
     // Camel: Assuming studentDetails is in body:
     // .bean(DwOperatorsBean3.class, "getDistinctSubjects(${body})").marshal().json();
     ```

---

## Script 12 (from 1operators.xml)

**1. Source XML File & Component:**
   - File: `1operators.xml`
   - Mule Component Context: `Transform: endsWith` (Flow: `operators101`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
---
["mulesofts" endsWith  "fts"]
```

**3. Script's Purpose:**
   - Checks if the string "mulesofts" ends with "fts". DW outputs `[true]`.

**4. Translation Complexity:**
   - Simple.

**5. Proposed Java/Camel Translation Strategy:**
   - Java Bean method.
   - **Java Bean Suggestion:**
     ```java
     import java.util.Collections;
     import java.util.List;
     public class DwOperatorsBean4 {
         public List<Boolean> checkEndsWith() {
             return Collections.singletonList("mulesofts".endsWith("fts"));
         }
     }
     // Camel: .bean(DwOperatorsBean4.class, "checkEndsWith").marshal().json();
     ```

---

## Script 13 (from 1operators.xml)

**1. Source XML File & Component:**
   - File: `1operators.xml`
   - Mule Component Context: `Transform: find` (Flow: `operators101`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
---
["a", "manju", "c", "a"] find ("a")
```

**3. Script's Purpose:**
   - Finds all indices of the string "a" in the given array. DW outputs `[0, 3]`.

**4. Translation Complexity:**
   - Moderate.

**5. Proposed Java/Camel Translation Strategy:**
   - Java Bean method to iterate and collect indices.
   - **Java Bean Suggestion:**
     ```java
     import java.util.*;
     public class DwOperatorsBean5 {
         public List<Integer> findStringIndices() {
             List<String> list = Arrays.asList("a", "manju", "c", "a");
             String target = "a";
             List<Integer> indices = new ArrayList<>();
             for (int i = 0; i < list.size(); i++) {
                 if (target.equals(list.get(i))) {
                     indices.add(i);
                 }
             }
             return indices;
         }
     }
     // Camel: .bean(DwOperatorsBean5.class, "findStringIndices").marshal().json();
     ```

---

## Script 14 (from 1operators.xml)

**1. Source XML File & Component:**
   - File: `1operators.xml`
   - Mule Component Context: `Transform: indexOf` (Flow: `Operators102`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
---
{
	"key1": ["a", "b", "c", "d"] indexOf  "u",
	"key2": "manjukaushik" indexOf  "k"
}
```

**3. Script's Purpose:**
   - Finds the first index of "u" in an array and "k" in a string.

**4. Translation Complexity:**
   - Simple.

**5. Proposed Java/Camel Translation Strategy:**
   - Java Bean method using `List.indexOf()` and `String.indexOf()`.
   - **Java Bean Suggestion:**
     ```java
     import java.util.*;
     public class DwOperatorsBean6 {
         public Map<String, Integer> findIndices() {
             Map<String, Integer> result = new HashMap<>();
             result.put("key1", Arrays.asList("a", "b", "c", "d").indexOf("u")); // Will be -1
             result.put("key2", "manjukaushik".indexOf("k")); // Will be 5
             return result;
         }
     }
     // Camel: .bean(DwOperatorsBean6.class, "findIndices").marshal().json();
     ```

---

## Script 15 (from 1operators.xml)

**1. Source XML File & Component:**
   - File: `1operators.xml`
   - Mule Component Context: `Transform: isUsage` (Flow: `Operators102`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
---
{
	"isblanks":{ "isBlank1": isBlank(""), "isBlank2": isBlank("MULE"), "isBlank3": isBlank(null), "isBlank4": isBlank('') },
	"isdecimals": { "isdecmial1": isDecimal(1.2), "isdecmial2": isDecimal(1) },
	"isEmpty": { "isEmpty1": isEmpty([]), "isEmpty2": isEmpty({}), "isEmpty3": isEmpty(null), "isEmpty4": isEmpty(""), "isEmpty5": isEmpty(''), "isEmpty6": isEmpty("Mule") }
}
```

**3. Script's Purpose:**
   - Demonstrates various type checking and emptiness/blankness checks.

**4. Translation Complexity:**
   - Moderate: Multiple checks, some require external libraries or careful null handling.

**5. Proposed Java/Camel Translation Strategy:**
   - Java Bean. Use `org.apache.commons.lang3.StringUtils.isBlank()` for `isBlank`. For `isDecimal`, check `instanceof Double || instanceof BigDecimal`. For `isEmpty`, check `value == null` or `value.isEmpty()` for Strings, Collections, Maps.
   - **Java Bean Suggestion (Partial):**
     ```java
     import org.apache.commons.lang3.StringUtils; // Add dependency if not present
     import java.util.*;
     public class DwOperatorsBean7 {
         public Map<String, Object> checkAllTypes() {
             Map<String, Object> result = new HashMap<>();
             Map<String, Boolean> isBlanks = new HashMap<>();
             isBlanks.put("isBlank1", StringUtils.isBlank(""));
             isBlanks.put("isBlank2", StringUtils.isBlank("MULE"));
             isBlanks.put("isBlank3", StringUtils.isBlank(null));
             isBlanks.put("isBlank4", StringUtils.isBlank(""));
             result.put("isblanks", isBlanks);
             // ... similar logic for isdecimals and isEmpty ...
             // For isEmpty({}): new HashMap<>().isEmpty()
             // For isEmpty([]): new ArrayList<>().isEmpty()
             return result;
         }
     }
     // Camel: .bean(DwOperatorsBean7.class, "checkAllTypes").marshal().json();
     ```

---

## Script 16 (from 1operators.xml)

**1. Source XML File & Component:**
   - File: `1operators.xml`
   - Mule Component Context: `Transform: minAndMax` (Flow: `Operators102`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/dw 
---
{
	"maximum": max([1,1000, 234, 999, 1000.1]),
	"minimum": min([1,1000, 234, 999, 1000.1, 0.2])
}
```

**3. Script's Purpose:**
   - Finds the maximum and minimum values in an array of numbers. Output `application/dw` usually means native Java objects if not further specified, but here it's likely a map for JSON.

**4. Translation Complexity:**
   - Simple.

**5. Proposed Java/Camel Translation Strategy:**
   - Java Bean using `Collections.max/min` or Stream API after converting to a common Number type like `Double`.
   - **Java Bean Suggestion:**
     ```java
     import java.util.*;
     import java.util.stream.Collectors;
     public class DwOperatorsBean8 {
         public Map<String, Number> getMinAndMax() {
             List<Double> numbers = Arrays.asList(1.0, 1000.0, 234.0, 999.0, 1000.1, 0.2);
             Map<String, Number> result = new HashMap<>();
             result.put("maximum", numbers.stream().mapToDouble(Double::doubleValue).max().orElse(Double.NaN));
             result.put("minimum", numbers.stream().mapToDouble(Double::doubleValue).min().orElse(Double.NaN));
             return result;
         }
     }
     // Camel: .bean(DwOperatorsBean8.class, "getMinAndMax").marshal().json();
     ```

---

## Script 17 (from 1operators.xml)

**1. Source XML File & Component:**
   - File: `1operators.xml`
   - Mule Component Context: `Transform: mod` (Flow: `Operators102`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/dw
---
[(3 mod 2), (4 mod 2), (2 mod 2), (1 mod 2), (0.75 mod 2)]
```

**3. Script's Purpose:**
   - Calculates modulo for several numbers.

**4. Translation Complexity:**
   - Simple.

**5. Proposed Java/Camel Translation Strategy:**
   - Java Bean using the `%` operator.
   - **Java Bean Suggestion:**
     ```java
     import java.util.*;
     public class DwOperatorsBean9 {
         public List<Number> calculateModulos() {
             return Arrays.asList(
                 (3 % 2), 
                 (4 % 2), 
                 (2 % 2), 
                 (1 % 2), 
                 (0.75 % 2) // Result of double % int is double
             );
         }
     }
     // Camel: .bean(DwOperatorsBean9.class, "calculateModulos").marshal().json();
     ```

---

## Script 18 (from 1operators.xml)

**1. Source XML File & Component:**
   - File: `1operators.xml`
   - Mule Component Context: `Transform: **Flatten**` (Flow: `Operators102`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
var A1 = [1,2,3,4]; var A2 = [7,8,6,9,10]; var A3 = [12,14,19,25];
var consolidateArray = [A1, A2, A3]; // This var is not used in the final output expression
var studentDetails = { userDetails: [ { "myName": "Manju_Kaushik", "toalSemesterCompleted":[ { "semester": "1", "yearofpassing": "2005" }, { "semester": "2", "yearofpassing": "2006" } ] }, { "myName": "Rahul_Kaushik", "toalSemesterCompleted":[ { "semester": "3", "yearofpassing": "2005" }, { "semester": "4", "yearofpassing": "2006" } ] } ] };
---
{ "key1": flatten(studentDetails.userDetails.toalSemesterCompleted) }
```

**3. Script's Purpose:**
   - Flattens a nested list: `studentDetails.userDetails.toalSemesterCompleted` (which is a list of lists of semesters).

**4. Translation Complexity:**
   - Moderate.

**5. Proposed Java/Camel Translation Strategy:**
   - Java Bean using Java Streams API `flatMap`.
   - **Java Bean Suggestion:**
     ```java
     import java.util.*;
     import java.util.stream.Collectors;
     public class DwOperatorsBean10 {
         @SuppressWarnings("unchecked")
         public Map<String, List<Object>> flattenSemesters(Map<String, Object> studentDetails) {
             List<Map<String, Object>> userDetails = (List<Map<String, Object>>) studentDetails.get("userDetails");
             List<Object> flattenedList = userDetails.stream()
                 .flatMap(ud -> ((List<Object>) ud.get("toalSemesterCompleted")).stream())
                 .collect(Collectors.toList());
             return Collections.singletonMap("key1", flattenedList);
         }
     }
     // Camel: Assuming studentDetails (as a Map) is in body:
     // .bean(DwOperatorsBean10.class, "flattenSemesters(${body})").marshal().json();
     ```

---

## Script 19 (from 1operators.xml)

**1. Source XML File & Component:**
   - File: `1operators.xml`
   - Mule Component Context: `Transform: **filter**` (Flow: `Operators102`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
---
[ { "name": "manju", "id": 1 }, { "name": "rahul", "id": 2 } ] filter ((item) -> item.id == 1)
```

**3. Script's Purpose:**
   - Filters a hardcoded list of maps where `id == 1`.

**4. Translation Complexity:**
   - Simple to Moderate (for the general case of filtering a list of maps).

**5. Proposed Java/Camel Translation Strategy:**
   - Java Bean using Java Streams API `filter`.
   - **Java Bean Suggestion:**
     ```java
     import java.util.*;
     import java.util.stream.Collectors;
     public class DwOperatorsBean11 {
         public List<Map<String, Object>> filterListById() {
             List<Map<String, Object>> data = Arrays.asList(
                 Map.of("name", "manju", "id", 1),
                 Map.of("name", "rahul", "id", 2)
             );
             return data.stream()
                        .filter(item -> item.containsKey("id") && ((Number)item.get("id")).intValue() == 1)
                        .collect(Collectors.toList());
         }
     }
     // Camel: .bean(DwOperatorsBean11.class, "filterListById").marshal().json();
     ```

---

## Script 20 (from 1operators.xml)

**1. Source XML File & Component:**
   - File: `1operators.xml`
   - Mule Component Context: `Transform: $-$$-$$$` (Flow: `Operators102`)

**2. DataWeave Script Content:**
```dw
%dw 2.0
output application/json
var numbers = (6 to 15) // Not used in the final expression that refers to payload
---
payload default [] filter $.empsalary > 1000
```

**3. Script's Purpose:**
   - Filters the input `payload` (assumed to be a list of objects/maps) where the `empsalary` field is greater than 1000. Includes a default empty list if payload is null.

**4. Translation Complexity:**
   - Moderate.

**5. Proposed Java/Camel Translation Strategy:**
   - Java Bean method. The input `payload` would be passed as an argument (e.g., `List<Map<String, Object>>` or `List<EmployeePojo>`).
   - **Java Bean Suggestion:**
     ```java
     import java.util.*;
     import java.util.stream.Collectors;

     // Assuming EmployeePojo or Map<String, Object> for payload elements
     // public class EmployeePojo { private double empsalary; /* getters/setters */ }

     public class DwOperatorsBean12 {
         @SuppressWarnings("unchecked")
         public List<Object> filterBySalary(Object payload) {
             List<Object> inputList;
             if (payload instanceof List) {
                 inputList = (List<Object>) payload;
             } else {
                 inputList = Collections.emptyList();
             }

             return inputList.stream().filter(item -> {
                 if (item instanceof Map) {
                     Object salary = ((Map<?, ?>) item).get("empsalary");
                     return salary instanceof Number && ((Number) salary).doubleValue() > 1000;
                 }
                 // else if (item instanceof EmployeePojo) { // If using POJOs
                 //    return ((EmployeePojo)item).getEmpsalary() > 1000;
                 // }
                 return false;
             }).collect(Collectors.toList());
         }
     }
     // Camel: .bean(DwOperatorsBean12.class, "filterBySalary(${body})").marshal().json();
     ```

---
