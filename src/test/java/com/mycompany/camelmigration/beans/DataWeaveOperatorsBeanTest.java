package com.mycompany.camelmigration.beans;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
}
