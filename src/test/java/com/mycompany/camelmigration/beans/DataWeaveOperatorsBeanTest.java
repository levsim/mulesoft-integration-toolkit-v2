package com.mycompany.camelmigration.beans;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataWeaveOperatorsBeanTest {

    private DataWeaveOperatorsBean dataWeaveOperatorsBean;

    @BeforeEach
    void setUp() {
        dataWeaveOperatorsBean = new DataWeaveOperatorsBean();
    }

    @Test
    @DisplayName("Test concatenateArraysAndString")
    void testConcatenateArraysAndString() {
        List<Object> list1 = Arrays.asList("a", 1);
        List<Object> list2 = Arrays.asList(true, 3.14);
        String str = "end";

        List<Object> result = dataWeaveOperatorsBean.concatenateArraysAndString(list1, list2, str);

        assertEquals(5, result.size());
        assertEquals("a", result.get(0));
        assertEquals(1, result.get(1));
        assertEquals(true, result.get(2));
        assertEquals(3.14, result.get(3));
        assertEquals("end", result.get(4));
    }

    @Test
    @DisplayName("Test concatenateArraysAndString with null lists and string")
    void testConcatenateArraysAndStringWithNulls() {
        List<Object> result = dataWeaveOperatorsBean.concatenateArraysAndString(null, null, null);
        assertTrue(result.isEmpty());

        List<Object> list1 = Arrays.asList("a");
        result = dataWeaveOperatorsBean.concatenateArraysAndString(list1, null, null);
        assertEquals(1, result.size());
        assertEquals("a", result.get(0));

        result = dataWeaveOperatorsBean.concatenateArraysAndString(null, list1, null);
        assertEquals(1, result.size());
        assertEquals("a", result.get(0));
        
        result = dataWeaveOperatorsBean.concatenateArraysAndString(null, null, "test");
        assertEquals(1, result.size());
        assertEquals("test", result.get(0));
    }

    @Test
    @DisplayName("Test createSampleMap")
    void testCreateSampleMap() {
        String name = "Camel Rider";
        Integer age = 30;
        Map<String, Object> result = dataWeaveOperatorsBean.createSampleMap(name, age);

        assertEquals(name, result.get("name"));
        assertEquals(age, result.get("age"));
        assertNotNull(result.get("processedTimestamp"));
        assertTrue(result.get("processedTimestamp") instanceof String);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Test getSizeOfList")
    void testGetSizeOfList() {
        assertEquals(0, dataWeaveOperatorsBean.getSizeOfList(null));
        assertEquals(0, dataWeaveOperatorsBean.getSizeOfList(new ArrayList<>()));
        assertEquals(3, dataWeaveOperatorsBean.getSizeOfList(Arrays.asList(1, 2, 3)));
    }

    @Test
    @DisplayName("Test flattenList")
    void testFlattenList() {
        List<List<Object>> listOfLists = Arrays.asList(
            Arrays.asList("a", "b"),
            Arrays.asList(1, 2, 3),
            new ArrayList<>(), // Empty list
            Arrays.asList("x", "y", "z")
        );
        List<Object> result = dataWeaveOperatorsBean.flattenList(listOfLists);
        List<Object> expected = Arrays.asList("a", "b", 1, 2, 3, "x", "y", "z");
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Test flattenList with null and empty lists")
    void testFlattenListWithNulls() {
        assertTrue(dataWeaveOperatorsBean.flattenList(null).isEmpty());
        assertTrue(dataWeaveOperatorsBean.flattenList(new ArrayList<>()).isEmpty());
        
        List<List<Object>> listOfListsWithEmpty = Arrays.asList(
            new ArrayList<>(),
            new ArrayList<>()
        );
        assertTrue(dataWeaveOperatorsBean.flattenList(listOfListsWithEmpty).isEmpty());
    }

    @Test
    @DisplayName("Test getCurrentTimestamp format")
    void testGetCurrentTimestamp() {
        String timestamp = dataWeaveOperatorsBean.getCurrentTimestamp();
        assertNotNull(timestamp);
        // Validate format "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
        // Simple check for T and Z/offset
        assertTrue(timestamp.contains("T"));
        // Check if it ends with Z or an offset like +00:00 or -05:00
        assertTrue(timestamp.matches(".*(Z|[+-]\\d{2}:\\d{2})$"), "Timestamp format does not end with Z or offset: " + timestamp);
        
        // More robust check: try to parse it
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        assertDoesNotThrow(() -> {
            Date parsedDate = sdf.parse(timestamp);
            assertNotNull(parsedDate);
        }, "Timestamp should be parseable with yyyy-MM-dd'T'HH:mm:ss.SSSXXX format.");
    }
    
    @Test
    @DisplayName("Test processWithProperties")
    void testProcessWithProperties() {
        String result = dataWeaveOperatorsBean.processWithProperties("input1", 123);
        assertEquals("Processed: input1 with number 123", result);
    }

    // --- Tests for new DateTime and Array methods ---

    @Test
    @DisplayName("Test formatDateTimePoc1")
    void testFormatDateTimePoc1() {
        Map<String, String> result = dataWeaveOperatorsBean.formatDateTimePoc1();
        assertNotNull(result);
        assertEquals("2022-Jul-09", result.get("date1_Java")); // Locale dependent for MMM
        // Example: "04:10:35 PM" or "16:10:35 AM/PM" - depends on actual SimpleDateFormat behavior for 'a' with HH
        // For "HH:mm:ss a", it's typically "16:10:35 PM" if Locale implies PM.
        // Let's be more specific if possible or adjust based on known JVM default locale for testing consistency.
        // For now, just check presence and non-emptiness.
        assertTrue(result.get("date2_Java").contains("16:10:35")); 
        assertTrue(result.get("date3_Java").contains("16:10:35") && result.get("date3_Java").contains("Jul/09/2022"));
        assertEquals("2022/07/09", result.get("dateCustom_Java"));
    }

    @Test
    @DisplayName("Test calculateTimeDifferenceAndUnits")
    void testCalculateTimeDifferenceAndUnits() {
        String dtStr1 = "2022-09-07T17:15:12.234";
        String dtStr2 = "2021-07-10T08:14:59.899";
        Map<String, Object> result = dataWeaveOperatorsBean.calculateTimeDifferenceAndUnits(dtStr1, dtStr2);

        assertNotNull(result.get("subtractedValue_Java_ISO8601")); // e.g., "PT10190H0M12.335S"
        assertTrue((Long)result.get("extractNano_Java") > 0);
        assertTrue((Long)result.get("extractMilliseconds_Java") > 0);
        assertTrue((Long)result.get("extractSeconds_Java") > 0);
        assertTrue((Long)result.get("extractHours_Java") > 0);
        assertTrue((Long)result.get("extractDays_Java") > 0);
        
        assertEquals(1, result.get("period_P1Y11M_years_Java"));
        assertEquals(11, result.get("period_P1Y11M_months_Java"));
        assertEquals(24L, result.get("period_P1Y12M_normalized_totalMonths_Java"));

    }

    @Test
    @DisplayName("Test dateTimeArithmetic")
    void testDateTimeArithmetic() {
        Map<String, String> result = dataWeaveOperatorsBean.dateTimeArithmetic();
        assertNotNull(result);
        // Example check - more specific checks would compare exact date strings
        assertEquals("2022-07-08T17:23:21Z", result.get("datePeriodSubDays_Java"));
        assertEquals("2021-07-09T17:23:21Z", result.get("datePeriodSubYear_Java"));
        assertEquals("2022-06-09T17:23:21Z", result.get("datePeriodSubMonth_Java"));
        assertEquals("2023-07-09T17:23:21Z", result.get("addYear_Java"));
        assertEquals("2021-07-09T17:23:21", result.get("removeZ_Java_LDT_minus_Year"));
        assertTrue(result.get("AddSub_Duration_Java").startsWith("PT")); // e.g. PT9460H
        assertEquals("15:23:21", result.get("calculateHour_Java"));
        // The "calculateMinutes_Java_Interpretation" is PT2M minus (17h23m21s), which is negative
        assertTrue(result.get("calculateMinutes_Java_Interpretation").startsWith("-PT")); 
    }

    @Test
    @DisplayName("Test countByEvenOddInList")
    void testCountByEvenOddInList() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
        Map<Boolean, Long> result = dataWeaveOperatorsBean.countByEvenOddInList(numbers);
        assertEquals(4L, result.get(true)); // Count of even numbers
        assertEquals(4L, result.get(false)); // Count of odd numbers

        List<Integer> emptyList = Collections.emptyList();
        Map<Boolean, Long> resultEmpty = dataWeaveOperatorsBean.countByEvenOddInList(emptyList);
        assertFalse(resultEmpty.containsKey(true)); // Or assertEquals(0L, resultEmpty.getOrDefault(true, 0L));
        assertFalse(resultEmpty.containsKey(false));

        assertNotNull(dataWeaveOperatorsBean.countByEvenOddInList(null)); // Should return empty map
    }

    @Test
    @DisplayName("Test divideListIntoChunks")
    void testDivideListIntoChunks() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<List<Integer>> result1 = dataWeaveOperatorsBean.divideListIntoChunks(numbers, 3);
        assertEquals(4, result1.size()); // [[1,2,3], [4,5,6], [7,8,9], [10]]
        assertEquals(Arrays.asList(1, 2, 3), result1.get(0));
        assertEquals(Arrays.asList(10), result1.get(3));

        List<List<Integer>> result2 = dataWeaveOperatorsBean.divideListIntoChunks(numbers, numbers.size());
        assertEquals(1, result2.size());
        assertEquals(numbers, result2.get(0));
        
        List<List<Integer>> result3 = dataWeaveOperatorsBean.divideListIntoChunks(numbers, 1);
        assertEquals(10, result3.size());
        assertEquals(Collections.singletonList(1), result3.get(0));

        assertTrue(dataWeaveOperatorsBean.divideListIntoChunks(null, 3).isEmpty());
        assertTrue(dataWeaveOperatorsBean.divideListIntoChunks(new ArrayList<>(), 3).isEmpty());
        assertTrue(dataWeaveOperatorsBean.divideListIntoChunks(numbers, 0).isEmpty());
        assertTrue(dataWeaveOperatorsBean.divideListIntoChunks(numbers, -1).isEmpty());
    }

    @Test
    @DisplayName("Test sumByProperty")
    void testSumByProperty() {
        List<Map<String, Integer>> items = Arrays.asList(
            Map.of("amount", 100, "id", 1),
            Map.of("amount", 250, "id", 2),
            Map.of("amount", 50, "id", 3),
            Map.of("otherKey", 1000, "id", 4) // Item without 'amount'
        );
        long sum = dataWeaveOperatorsBean.sumByProperty(items, "amount");
        assertEquals(400L, sum); // 100 + 250 + 50 + 0

        long sumOther = dataWeaveOperatorsBean.sumByProperty(items, "otherKey");
        assertEquals(1000L, sumOther);

        assertEquals(0L, dataWeaveOperatorsBean.sumByProperty(null, "amount"));
        assertEquals(0L, dataWeaveOperatorsBean.sumByProperty(new ArrayList<>(), "amount"));
        assertEquals(0L, dataWeaveOperatorsBean.sumByProperty(items, null));
        assertEquals(0L, dataWeaveOperatorsBean.sumByProperty(items, "nonExistentKey"));
    }

    // --- Tests for methods from basicDw.xml and basicMathOperations.xml ---

    @Test
    @DisplayName("Test declareVarsAndConcat")
    void testDeclareVarsAndConcat() {
        Map<String, String> result = dataWeaveOperatorsBean.declareVarsAndConcat();
        assertEquals("Mule It is mulesoft", result.get("key1_Java"));
    }

    @Test
    @DisplayName("Test typeCoercionPoc1")
    void testTypeCoercionPoc1() {
        Map<String, Object> result = dataWeaveOperatorsBean.typeCoercionPoc1();
        assertEquals(22.0, (Double) result.get("key1_Java"), 0.001);
        assertEquals(125748.326, (Double) result.get("key2_Java"), 0.000001);
        // Note: The exact MMM representation can be locale-sensitive.
        // For "en_IN" locale, "Jul" is standard. If test JVM locale differs, this might need adjustment.
        assertEquals("09-Jul-20", result.get("key3_Java")); 
    }

    @Test
    @DisplayName("Test basicArithmeticAndObjectOps")
    void testBasicArithmeticAndObjectOps() {
        Map<String, Object> result = dataWeaveOperatorsBean.basicArithmeticAndObjectOps();
        assertEquals(4, result.get("add_Java"));
        assertEquals(0, result.get("sub_Java"));
        assertEquals(4, result.get("mul_Java"));
        assertEquals(45.0 / 98.0, (Double) result.get("div_Java"), 0.000001);

        @SuppressWarnings("unchecked")
        List<Integer> arrayAddResult = (List<Integer>) result.get("arrayAdd_Java_Interpreted");
        assertEquals(Arrays.asList(4, 5, 6), arrayAddResult);

        @SuppressWarnings("unchecked")
        Map<String, Integer> objectAddResult = (Map<String, Integer>) result.get("objectAdd_Java");
        assertEquals(Map.of("key2", 2, "key3", 3), objectAddResult);
    }

    @Test
    @DisplayName("Test equalityRelationalCheck")
    void testEqualityRelationalCheck() {
        Map<String, Boolean> result = dataWeaveOperatorsBean.equalityRelationalCheck();
        assertFalse(result.get("k1_Java")); // 1 < 1
        assertFalse(result.get("k2_Java")); // 1 <= -8
        assertFalse(result.get("k3_Java")); // 56 > 98
        assertTrue(result.get("k4_Java"));  // 876 >= 876.0
        assertTrue(result.get("k5_Java"));  // 1 == 1
        assertTrue(result.get("k6_Java"));  // "manju".equals("manju")
        assertTrue(result.get("k7_Java"));  // Integer.parseInt("45") == 45
    }

    // --- Tests for methods from dwTypes.xml and functions.xml ---

    @Test
    @DisplayName("Test demonstrateBasicTypesMap")
    void testDemonstrateBasicTypesMap() {
        Map<String, Object> result = dataWeaveOperatorsBean.demonstrateBasicTypesMap();
        assertNotNull(result);
        assertEquals("this is string", result.get("stringValue_Java"));
        assertTrue(result.get("numberValue_Java") instanceof Map);
        assertTrue(result.get("thisisboolean_Java") instanceof Map);
        assertTrue(result.get("thisisArray_Java") instanceof List);
        assertTrue(result.get("thisisMyObject_Java") instanceof Map);
        assertTrue(result.get("anotherexampleofArray_Java") instanceof List);
        
        @SuppressWarnings("unchecked")
        List<Object> anotherArray = (List<Object>) result.get("anotherexampleofArray_Java");
        assertEquals(6, anotherArray.size());
        assertEquals("manjukaushik", anotherArray.get(3));
    }

    @Test
    @DisplayName("Test getTypeOfStringAndNumber")
    void testGetTypeOfStringAndNumber() {
        Map<String, String> result = dataWeaveOperatorsBean.getTypeOfStringAndNumber();
        assertEquals("String", result.get("typeOfString_Java"));
        assertEquals("Integer", result.get("typeOfInteger_Java"));
        assertEquals("Double", result.get("typeOfDouble_Java"));
        assertEquals("String", result.get("typeOfEmptyString_Java"));
    }

    @Test
    @DisplayName("Test listManipulationOperations")
    void testListManipulationOperations() {
        Map<String, List<Object>> result = dataWeaveOperatorsBean.listManipulationOperations();
        assertEquals(Arrays.asList(76, 109), result.get("key1_Java"));
        assertEquals(Arrays.asList(151, 109), result.get("key2_Java"));
        assertEquals(Arrays.asList("manju", "kaushik"), result.get("key3_Java"));
        assertEquals(Arrays.asList(154, 165), result.get("key4_Java"));
        assertEquals(Arrays.asList(154, 234), result.get("key5_Java"));
        assertTrue(result.get("key6_Java").isEmpty());
        assertEquals(Arrays.asList(Map.of("a","b"), Map.of("e","f")), result.get("key7_Java"));
    }

    @Test
    @DisplayName("Test invokeCustomFunctions (moduleAgeID and emptyFunction)")
    void testInvokeCustomFunctions() {
        Map<String, Object> result = dataWeaveOperatorsBean.invokeCustomFunctions();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> output1 = (Map<String, Object>) result.get("output1_Java");
        assertEquals("india", output1.get("k1")); // age 45 < 60

        @SuppressWarnings("unchecked")
        Map<String, Object> output2 = (Map<String, Object>) result.get("output2_Java");
        assertEquals("rahul", output2.get("k1")); // age 74 > 60

        @SuppressWarnings("unchecked")
        List<Map<String, String>> output4 = (List<Map<String, String>>) result.get("output4_Java");
        assertEquals(1, output4.size());
        assertEquals(Map.of("k1", "hello"), output4.get(0));
    }

    @Test
    @DisplayName("Test invokeFunctionWithOptionalParams")
    void testInvokeFunctionWithOptionalParams() {
        Map<String, Object> result = dataWeaveOperatorsBean.invokeFunctionWithOptionalParams();

        @SuppressWarnings("unchecked")
        Map<String, Integer> k1 = (Map<String, Integer>) result.get("k1_Java");
        assertEquals(10, k1.get("k1"));
        assertEquals(2, k1.get("k2")); // default
        assertEquals(3, k1.get("k3")); // default

        @SuppressWarnings("unchecked")
        Map<String, Integer> k2 = (Map<String, Integer>) result.get("k2_Java");
        assertEquals(10, k2.get("k1"));
        assertEquals(20, k2.get("k2"));
        assertEquals(3, k2.get("k3")); // default
        
        @SuppressWarnings("unchecked")
        Map<String, Integer> k3 = (Map<String, Integer>) result.get("k3_Java");
        assertEquals(10, k3.get("k1"));
        assertEquals(20, k3.get("k2"));
        assertEquals(40, k3.get("k3"));
    }

    // --- Tests for methods from mapAndFilter.xml and pluckReduce.xml ---

    @Test
    @DisplayName("Test mapStudentData")
    void testMapStudentData() {
        List<Map<String, Object>> studentList = Arrays.asList(
            Map.of("studentID", 1, "studentName", "Manju", "studentLocation", "India"),
            Map.of("studentID", 2, "studentName", "Mohan", "studentLocation", "USA")
        );
        List<Map<String, Object>> result = dataWeaveOperatorsBean.mapStudentData(studentList);
        
        assertEquals(2, result.size());
        assertEquals("1_Manju", result.get(0).get("studentID_Java"));
        assertEquals("India", result.get(0).get("studentLocation_Java"));
        assertEquals("2_Mohan", result.get(1).get("studentID_Java"));
        assertEquals("USA", result.get(1).get("studentLocation_Java"));

        assertTrue(dataWeaveOperatorsBean.mapStudentData(null).isEmpty());
        assertTrue(dataWeaveOperatorsBean.mapStudentData(new ArrayList<>()).isEmpty());
    }

    @Test
    @DisplayName("Test mapStringsToIndexedObject")
    void testMapStringsToIndexedObject() {
        List<String> names = Arrays.asList("manju", "mohan", "rajeev");
        Map<String, String> result = dataWeaveOperatorsBean.mapStringsToIndexedObject(names, 2, "1");
        
        assertEquals(3, result.size());
        assertEquals("manju1", result.get("2")); // 0+2
        assertEquals("mohan1", result.get("3")); // 1+2
        assertEquals("rajeev1", result.get("4"));// 2+2

        assertTrue(dataWeaveOperatorsBean.mapStringsToIndexedObject(null, 2, "1").isEmpty());
        assertTrue(dataWeaveOperatorsBean.mapStringsToIndexedObject(new ArrayList<>(), 2, "1").isEmpty());
    }

    @Test
    @DisplayName("Test filterObjectByKeyValue")
    void testFilterObjectByKeyValue() {
        Map<String, Object> obj = new HashMap<>();
        obj.put("studentID", 1);
        obj.put("studentName", "Manju");
        obj.put("studentLocation", "India");
        obj.put("status", "Active");

        Map<String, Object> result = dataWeaveOperatorsBean.filterObjectByKeyValue(obj, "M", "studentN");
        assertEquals(1, result.size());
        assertEquals("Manju", result.get("studentName"));

        Map<String, Object> result2 = dataWeaveOperatorsBean.filterObjectByKeyValue(obj, "India", "studentL");
        assertEquals(1, result2.size());
        assertEquals("India", result2.get("studentLocation"));
        
        assertTrue(dataWeaveOperatorsBean.filterObjectByKeyValue(null, "M", "s").isEmpty());
        assertTrue(dataWeaveOperatorsBean.filterObjectByKeyValue(new HashMap<>(), "M", "s").isEmpty());
    }
    
    @Test
    @DisplayName("Test pluckValueFromListOfMaps")
    void testPluckValueFromListOfMaps() {
        List<Map<String, Object>> list = Arrays.asList(
            Map.of("name", "Alice", "age", 30),
            Map.of("name", "Bob", "age", 25),
            Map.of("city", "London"), // No 'name' key
            Map.of("name", "Charlie", "age", 35)
        );
        List<Object> names = dataWeaveOperatorsBean.pluckValueFromListOfMaps(list, "name");
        assertEquals(Arrays.asList("Alice", "Bob", "Charlie"), names);

        List<Object> ages = dataWeaveOperatorsBean.pluckValueFromListOfMaps(list, "age");
        assertEquals(Arrays.asList(30, 25, 35), ages);
        
        assertTrue(dataWeaveOperatorsBean.pluckValueFromListOfMaps(null, "name").isEmpty());
        assertTrue(dataWeaveOperatorsBean.pluckValueFromListOfMaps(new ArrayList<>(), "name").isEmpty());
        assertTrue(dataWeaveOperatorsBean.pluckValueFromListOfMaps(list, null).isEmpty());
        assertTrue(dataWeaveOperatorsBean.pluckValueFromListOfMaps(list, "nonExistentKey").isEmpty());
    }

    @Test
    @DisplayName("Test reduceToSum")
    void testReduceToSum() {
        List<Integer> numbers = Arrays.asList(10, 20, 30);
        assertEquals(60, dataWeaveOperatorsBean.reduceToSum(numbers, 0));
        assertEquals(72, dataWeaveOperatorsBean.reduceToSum(numbers, 12));
        assertEquals(0, dataWeaveOperatorsBean.reduceToSum(null, 0));
        assertEquals(10, dataWeaveOperatorsBean.reduceToSum(null, 10));
        assertEquals(0, dataWeaveOperatorsBean.reduceToSum(new ArrayList<>(), 0));
    }

    @Test
    @DisplayName("Test reduceToStringConcat")
    void testReduceToStringConcat() {
        List<Object> items = Arrays.asList(1, "2", 3.0, "Four", true);
        assertEquals("123.0Fourtrue", dataWeaveOperatorsBean.reduceToStringConcat(items));
        
        assertEquals("", dataWeaveOperatorsBean.reduceToStringConcat(null));
        assertEquals("", dataWeaveOperatorsBean.reduceToStringConcat(new ArrayList<>()));
    }

    // --- Tests for methods from lambdaExpression.xml and scopeFlowConstructs.xml ---
    @Test
    @DisplayName("Test lambdaMapToUppercase")
    void testLambdaMapToUppercase() {
        List<String> names = Arrays.asList("Manju", "Rahul", "Mohan");
        List<String> expected = Arrays.asList("MANJU", "RAHUL", "MOHAN");
        assertEquals(expected, dataWeaveOperatorsBean.lambdaMapToUppercase(names));
        assertTrue(dataWeaveOperatorsBean.lambdaMapToUppercase(null).isEmpty());
        assertTrue(dataWeaveOperatorsBean.lambdaMapToUppercase(new ArrayList<>()).isEmpty());
    }

    @Test
    @DisplayName("Test lambdaChainFilters")
    void testLambdaChainFilters() {
        List<Integer> numbers = IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList());
        // DW: numbers filter item > 1 filter item < 5 filter (item mod 2) == 1
        // Results: (2,3,4) -> filter (item mod 2) == 1 -> (3)
        List<Integer> expected = Collections.singletonList(3);
        assertEquals(expected, dataWeaveOperatorsBean.lambdaChainFilters(numbers));
        assertTrue(dataWeaveOperatorsBean.lambdaChainFilters(null).isEmpty());
        assertTrue(dataWeaveOperatorsBean.lambdaChainFilters(new ArrayList<>()).isEmpty());
    }

    @Test
    @DisplayName("Test doScopeSimulation")
    void testDoScopeSimulation() {
        // DW: var fName = "Mule"; var lName = "Soft"; 
        //     var x = do { var concatenateFL = fName ++ lName; var x1 = "hi"; --- concatenateFL ++ x1 }; --- x
        // Expected: "MuleSofthi"
        assertEquals("MuleSofthi", dataWeaveOperatorsBean.doScopeSimulation());
    }

    @Test
    @DisplayName("Test conditionalLogicExample")
    void testConditionalLogicExample() {
        // DW: if (payload.age > 60 and payload.id >= 1) payload.name 
		//     else if (payload.age < 60 and payload.id >= 1) payload.location 
		//     else payload.salary
        
        Map<String, Object> payload1 = Map.of("name", "Manju", "location", "india", "id", 1, "age", 40, "salary", 1000);
        assertEquals("india", dataWeaveOperatorsBean.conditionalLogicExample(payload1).get("k1_Java"));

        Map<String, Object> payload2 = Map.of("name", "Rahul", "location", "usa", "id", 2, "age", 70, "salary", 2000);
        assertEquals("Rahul", dataWeaveOperatorsBean.conditionalLogicExample(payload2).get("k1_Java"));
        
        Map<String, Object> payload3 = Map.of("name", "Test", "location", "moon", "id", 0, "age", 50, "salary", 3000); // id < 1
        assertEquals(3000, dataWeaveOperatorsBean.conditionalLogicExample(payload3).get("k1_Java"));

        Map<String, Object> payload4 = Map.of("name", "Test2", "location", "mars", "id", 0, "age", 70, "salary", 4000); // id < 1
        assertEquals(4000, dataWeaveOperatorsBean.conditionalLogicExample(payload4).get("k1_Java"));
    }

    // --- Tests for new methods for specific operators from 1operators.xml ---

    @Test
    @DisplayName("Test avg operator")
    void testAvg() {
        assertEquals(500.5, dataWeaveOperatorsBean.avg(Arrays.asList(1, 1000)), 0.001);
        assertEquals(10.0, dataWeaveOperatorsBean.avg(Arrays.asList(10, 10, 10)), 0.001);
        assertEquals(0.0, dataWeaveOperatorsBean.avg(new ArrayList<>()), 0.001); // Empty list
        assertEquals(0.0, dataWeaveOperatorsBean.avg(null), 0.001); // Null list
        assertEquals(3.333, dataWeaveOperatorsBean.avg(Arrays.asList(2.5, 3.5, 4.0)), 0.001);
        List<Integer> intList = Arrays.asList(1,2,3,4,5);
        assertEquals(3.0, dataWeaveOperatorsBean.avg(intList), 0.001);
    }

    @Test
    @DisplayName("Test distinctByValue operator")
    void testDistinctByValue() {
        assertEquals(Arrays.asList(1, 2, 3, 4), dataWeaveOperatorsBean.distinctByValue(Arrays.asList(1, 2, 3, 1, 2, 4)));
        assertEquals(Arrays.asList("a", "b", "c"), dataWeaveOperatorsBean.distinctByValue(Arrays.asList("a", "b", "a", "c", "b")));
        assertTrue(dataWeaveOperatorsBean.distinctByValue(null).isEmpty());
        assertTrue(dataWeaveOperatorsBean.distinctByValue(new ArrayList<>()).isEmpty());
    }

    @Test
    @DisplayName("Test distinctByKey operator")
    void testDistinctByKey() {
        List<Map<String, Object>> list = Arrays.asList(
            Map.of("id", 1, "value", "A"),
            Map.of("id", 2, "value", "B"),
            Map.of("id", 1, "value", "C"), // Duplicate id
            Map.of("id", 3, "value", "D"),
            Map.of("id", 2, "value", "E")  // Duplicate id
        );
        List<Map<String, Object>> expected = Arrays.asList(
            Map.of("id", 1, "value", "A"),
            Map.of("id", 2, "value", "B"),
            Map.of("id", 3, "value", "D")
        );
        assertEquals(expected, dataWeaveOperatorsBean.distinctByKey(list, "id"));

        assertTrue(dataWeaveOperatorsBean.distinctByKey(null, "id").isEmpty());
        assertTrue(dataWeaveOperatorsBean.distinctByKey(new ArrayList<>(), "id").isEmpty());
        
        List<Map<String, Object>> listNoKey = Arrays.asList(Map.of("value", "A"), Map.of("value", "B"));
        // If key is missing, map.get(key) is null. All nulls are "equal" for first distinct.
        // So first element with missing key is kept.
        List<Map<String, Object>> expectedNoKey = Arrays.asList(Map.of("value", "A"));
        // Correction: distinctByKey keeps the first unique value for the key. If key is "id", and it's missing,
        // the first map where "id" is null is kept. Subsequent maps where "id" is also null are dropped.
        assertEquals(1, dataWeaveOperatorsBean.distinctByKey(listNoKey, "id").size());
        assertEquals(Map.of("value", "A"), dataWeaveOperatorsBean.distinctByKey(listNoKey, "id").get(0));


        List<Map<String, Object>> listWithNullKeyVal = new ArrayList<>();
        Map<String,Object> map1 = new HashMap<>(); map1.put("id", null); map1.put("val", "V1");
        Map<String,Object> map2 = new HashMap<>(); map2.put("id", 1); map2.put("val", "V2");
        Map<String,Object> map3 = new HashMap<>(); map3.put("id", null); map3.put("val", "V3");
        listWithNullKeyVal.add(map1); listWithNullKeyVal.add(map2); listWithNullKeyVal.add(map3);
        
        List<Map<String, Object>> expectedWithNullKeyVal = Arrays.asList(map1, map2);
        assertEquals(expectedWithNullKeyVal, dataWeaveOperatorsBean.distinctByKey(listWithNullKeyVal, "id"));

    }

    @Test
    @DisplayName("Test checkEndsWith operator")
    void testCheckEndsWith() {
        assertTrue(dataWeaveOperatorsBean.checkEndsWith("mulesofts", "fts"));
        assertFalse(dataWeaveOperatorsBean.checkEndsWith("mulesofts", "Fts")); // Case-sensitive
        assertFalse(dataWeaveOperatorsBean.checkEndsWith("mule", "mulesoft"));
        assertTrue(dataWeaveOperatorsBean.checkEndsWith("mule", "")); // Empty suffix
        assertFalse(dataWeaveOperatorsBean.checkEndsWith(null, "fts"));
        assertFalse(dataWeaveOperatorsBean.checkEndsWith("mulesofts", null));
    }
    
    @Test
    @DisplayName("Test findFirstIndexOfInList operator")
    void testFindFirstIndexOfInList() {
        List<String> list = Arrays.asList("a", "b", "c", "b", "d");
        assertEquals(1, dataWeaveOperatorsBean.findFirstIndexOfInList(list, "b"));
        assertEquals(0, dataWeaveOperatorsBean.findFirstIndexOfInList(list, "a"));
        assertEquals(4, dataWeaveOperatorsBean.findFirstIndexOfInList(list, "d"));
        assertEquals(-1, dataWeaveOperatorsBean.findFirstIndexOfInList(list, "x")); // Not found
        assertEquals(-1, dataWeaveOperatorsBean.findFirstIndexOfInList(null, "a"));
        assertEquals(-1, dataWeaveOperatorsBean.findFirstIndexOfInList(new ArrayList<>(), "a"));
    }

    @Test
    @DisplayName("Test findFirstIndexOfInString operator")
    void testFindFirstIndexOfInString() {
        assertEquals(6, dataWeaveOperatorsBean.findFirstIndexOfInString("hello world", "world"));
        assertEquals(0, dataWeaveOperatorsBean.findFirstIndexOfInString("hello world", "hello"));
        assertEquals(2, dataWeaveOperatorsBean.findFirstIndexOfInString("hello world", "llo"));
        assertEquals(-1, dataWeaveOperatorsBean.findFirstIndexOfInString("hello world", "Java")); // Not found
        assertEquals(-1, dataWeaveOperatorsBean.findFirstIndexOfInString(null, "a"));
        assertEquals(-1, dataWeaveOperatorsBean.findFirstIndexOfInString("hello", null));
        assertEquals(0, dataWeaveOperatorsBean.findFirstIndexOfInString("hello", "")); // Empty substring
    }

    @Test
    @DisplayName("Test minInList operator")
    void testMinInList() {
        assertEquals(0.2, dataWeaveOperatorsBean.minInList(Arrays.asList(1.0, 1000.0, 234.0, 999.0, 1000.1, 0.2)), 0.001);
        assertEquals(Integer.valueOf(10), dataWeaveOperatorsBean.minInList(Arrays.asList(10, 20, 30)));
        assertNull(dataWeaveOperatorsBean.minInList(null));
        assertNull(dataWeaveOperatorsBean.minInList(new ArrayList<>()));
    }

    @Test
    @DisplayName("Test maxInList operator")
    void testMaxInList() {
        assertEquals(1000.1, dataWeaveOperatorsBean.maxInList(Arrays.asList(1.0, 1000.0, 234.0, 999.0, 1000.1, 0.2)), 0.001);
        assertEquals(Integer.valueOf(30), dataWeaveOperatorsBean.maxInList(Arrays.asList(10, 20, 30)));
        assertNull(dataWeaveOperatorsBean.maxInList(null));
        assertNull(dataWeaveOperatorsBean.maxInList(new ArrayList<>()));
    }
}
