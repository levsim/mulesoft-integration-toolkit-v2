package com.mycompany.camelmigration.routes;

import com.mycompany.camelmigration.beans.DataWeaveOperatorsBean;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataWeaveOperatorsRouteBuilder extends RouteBuilder {

    @Autowired
    private DataWeaveOperatorsBean dataWeaveOperatorsBean;

    @Override
    public void configure() throws Exception {
        from("direct:testOperators")
            .routeId("test-dataweave-operators")
            .setProperty("myList1", () -> Arrays.asList(10, 20, 30)) // Using lambda for dynamic list creation
            .setProperty("myList2", () -> Arrays.asList("X", "Y"))
            .setProperty("myString", constant("Z99"))
            .log("Initial properties: myList1=${exchangeProperty.myList1}, myList2=${exchangeProperty.myList2}, myString=${exchangeProperty.myString}")

            // 1. Test concatenateArraysAndString
            .bean(dataWeaveOperatorsBean, "concatenateArraysAndString(${exchangeProperty.myList1}, ${exchangeProperty.myList2}, ${exchangeProperty.myString})")
            .log("concatenateArraysAndString result: ${body}")
            .setProperty("concatenatedList", simple("${body}")) // Save the result for next step

            // 2. Test getSizeOfList using the result from the previous step
            .bean(dataWeaveOperatorsBean, "getSizeOfList(${exchangeProperty.concatenatedList})")
            .log("getSizeOfList result (for concatenatedList): ${body}")

            // 3. Test createSampleMap
            .bean(dataWeaveOperatorsBean, "createSampleMap('Camel User', 5)")
            .log("createSampleMap result (raw map): ${body}")
            .marshal().json(JsonLibrary.Jackson) // Marshal the map to JSON
            .log("createSampleMap result (JSON): ${body}")
            
            // 4. Test getCurrentTimestamp
            .bean(dataWeaveOperatorsBean, "getCurrentTimestamp")
            .log("getCurrentTimestamp result: ${body}")

            // 5. Test flattenList (example setup)
            .process(e -> {
                List<List<Object>> listOfLists = Arrays.asList(
                    Arrays.asList("a", "b"),
                    Arrays.asList(1, 2, 3),
                    Arrays.asList("x", "y", "z")
                );
                e.setProperty("myListOfLists", listOfLists);
            })
            .bean(dataWeaveOperatorsBean, "flattenList(${exchangeProperty.myListOfLists})")
            .log("flattenList result: ${body}")

            // --- Demonstrating new DateTime and Array methods ---

            // A. DateTime Operations
            .log("--- Testing DateTime Operations ---")
            .bean(dataWeaveOperatorsBean, "formatDateTimePoc1")
            .marshal().json(JsonLibrary.Jackson)
            .log("formatDateTimePoc1 result: ${body}")

            .setProperty("dateTimeStr1", constant("2022-09-07T17:15:12.234"))
            .setProperty("dateTimeStr2", constant("2021-07-10T08:14:59.899"))
            .bean(dataWeaveOperatorsBean, "calculateTimeDifferenceAndUnits(${exchangeProperty.dateTimeStr1}, ${exchangeProperty.dateTimeStr2})")
            .marshal().json(JsonLibrary.Jackson)
            .log("calculateTimeDifferenceAndUnits result: ${body}")
            
            .bean(dataWeaveOperatorsBean, "dateTimeArithmetic")
            .marshal().json(JsonLibrary.Jackson)
            .log("dateTimeArithmetic result: ${body}")

            // B. Array Operations
            .log("--- Testing Array Operations ---")
            .setProperty("numberListForCount", () -> Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8))
            .bean(dataWeaveOperatorsBean, "countByEvenOddInList(${exchangeProperty.numberListForCount})")
            .marshal().json(JsonLibrary.Jackson)
            .log("countByEvenOddInList result: ${body}")

            .setProperty("numberListForDivide", () -> Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
            .bean(dataWeaveOperatorsBean, "divideListIntoChunks(${exchangeProperty.numberListForDivide}, 3)") // Chunk size 3
            .marshal().json(JsonLibrary.Jackson)
            .log("divideListIntoChunks (size 3) result: ${body}")
            
            .process(e -> {
                List<Map<String, Integer>> items = Arrays.asList(
                    Map.of("amount", 100, "id", 1),
                    Map.of("amount", 250, "id", 2),
                    Map.of("amount", 50, "id", 3)
                );
                e.setProperty("itemListForSum", items);
            })
            .bean(dataWeaveOperatorsBean, "sumByProperty(${exchangeProperty.itemListForSum}, 'amount')")
            .log("sumByProperty ('amount') result: ${body}")

            // --- Demonstrating new methods from dwTypes.xml and functions.xml ---
            .log("--- Testing Type and Function Operations ---")
            .bean(dataWeaveOperatorsBean, "demonstrateBasicTypesMap")
            .marshal().json(JsonLibrary.Jackson)
            .log("demonstrateBasicTypesMap result: ${body}")

            .bean(dataWeaveOperatorsBean, "getTypeOfStringAndNumber")
            .marshal().json(JsonLibrary.Jackson)
            .log("getTypeOfStringAndNumber result: ${body}")

            .bean(dataWeaveOperatorsBean, "listManipulationOperations")
            .marshal().json(JsonLibrary.Jackson)
            .log("listManipulationOperations result: ${body}")
            
            .bean(dataWeaveOperatorsBean, "invokeCustomFunctions")
            .marshal().json(JsonLibrary.Jackson)
            .log("invokeCustomFunctions result: ${body}")

            .bean(dataWeaveOperatorsBean, "invokeFunctionWithOptionalParams")
            .marshal().json(JsonLibrary.Jackson)
            .log("invokeFunctionWithOptionalParams result: ${body}")

            // --- Demonstrating new methods for 1operators.xml (avg, distinctBy, endsWith, indexOf, min, max) ---
            .log("--- Testing More 1operators.xml Methods ---")

            // avg
            .setProperty("numberListForAvg", () -> Arrays.asList(1, 2, 3, 4, 5, 5.5))
            .bean(dataWeaveOperatorsBean, "avg(${exchangeProperty.numberListForAvg})")
            .log("avg result: ${body}")

            // distinctByValue
            .setProperty("listForDistinctByValue", () -> Arrays.asList("apple", "banana", "orange", "apple", "grape", "banana"))
            .bean(dataWeaveOperatorsBean, "distinctByValue(${exchangeProperty.listForDistinctByValue})")
            .marshal().json(JsonLibrary.Jackson)
            .log("distinctByValue result: ${body}")

            // distinctByKey
            .process(e -> {
                List<Map<String, Object>> listMap = Arrays.asList(
                    Map.of("id", 1, "fruit", "apple"),
                    Map.of("id", 2, "fruit", "banana"),
                    Map.of("id", 1, "fruit", "apple_duplicate"), // Duplicate id
                    Map.of("id", 3, "fruit", "orange")
                );
                e.setProperty("listForDistinctByKey", listMap);
            })
            .bean(dataWeaveOperatorsBean, "distinctByKey(${exchangeProperty.listForDistinctByKey}, 'id')")
            .marshal().json(JsonLibrary.Jackson)
            .log("distinctByKey ('id') result: ${body}")

            // checkEndsWith
            .bean(dataWeaveOperatorsBean, "checkEndsWith('hello.txt', '.txt')")
            .log("checkEndsWith ('hello.txt', '.txt') result: ${body}")

            // findFirstIndexOfInList
            .setProperty("listForIndexOf", () -> Arrays.asList("one", "two", "three", "two"))
            .bean(dataWeaveOperatorsBean, "findFirstIndexOfInList(${exchangeProperty.listForIndexOf}, 'two')")
            .log("findFirstIndexOfInList ('two') result: ${body}")
            
            // findFirstIndexOfInString
            .bean(dataWeaveOperatorsBean, "findFirstIndexOfInString('hello camel world', 'camel')")
            .log("findFirstIndexOfInString ('camel') result: ${body}")

            // minInList
            .setProperty("numberListForMinMax", () -> Arrays.asList(5, 1, 9, 2.5, 0.5, 10))
            .bean(dataWeaveOperatorsBean, "minInList(${exchangeProperty.numberListForMinMax})")
            .log("minInList result: ${body}")
            
            // maxInList
            .bean(dataWeaveOperatorsBean, "maxInList(${exchangeProperty.numberListForMinMax})")
            .log("maxInList result: ${body}")
            
            .setBody(simple("Successfully tested all DataWeave operator bean methods. Final logged body is from maxInList."));
    }
}
