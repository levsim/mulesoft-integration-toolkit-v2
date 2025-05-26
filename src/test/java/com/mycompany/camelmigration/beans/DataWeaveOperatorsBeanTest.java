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
}
