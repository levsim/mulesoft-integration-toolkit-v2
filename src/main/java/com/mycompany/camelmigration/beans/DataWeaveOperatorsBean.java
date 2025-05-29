package com.mycompany.camelmigration.beans;

import org.springframework.stereotype.Component;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Objects;


@Component("dataWeaveOperatorsBean")
public class DataWeaveOperatorsBean {

    /**
     * Concatenates two lists and adds a string element.
     * Mimics DataWeave: list1 ++ list2 ++ "someString"
     */
    public List<Object> concatenateArraysAndString(List<Object> list1, List<Object> list2, String str) {
        List<Object> result = new ArrayList<>();
        if (list1 != null) {
            result.addAll(list1);
        }
        if (list2 != null) {
            result.addAll(list2);
        }
        if (str != null) {
            result.add(str);
        }
        return result;
    }

    /**
     * Constructs a simple Java Map.
     * Mimics DataWeave: { name: "somename", age: someage }
     */
    public Map<String, Object> createSampleMap(String name, Integer age) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("age", age);
        map.put("processedTimestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        return map;
    }

    /**
     * Returns the size of a list.
     * Mimics DataWeave: sizeOf(list)
     */
    public int getSizeOfList(List<?> list) {
        return list == null ? 0 : list.size();
    }

    /**
     * Flattens a list of lists into a single list.
     * Mimics DataWeave: flatten(listOfLists)
     */
    public List<Object> flattenList(List<List<Object>> listOfLists) {
        if (listOfLists == null) {
            return new ArrayList<>();
        }
        return listOfLists.stream()
                          .flatMap(List::stream)
                          .collect(Collectors.toList());
    }

    /**
     * Returns the current timestamp as a formatted string.
     * Mimics DataWeave: now() (though DW now() returns a DateTime object)
     */
    public String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return sdf.format(new Date());
    }

    /**
     * Example method to demonstrate calling with Exchange properties.
     * (Not directly from the list, but useful for testing).
     */
    public String processWithProperties(String input1, int input2) {
        return "Processed: " + input1 + " with number " + input2;
    }

    // --- New methods based on DateTime.xml ---

    /**
     * Mimics DataWeave's DateTime formatting (POC1 and POC2 from DateTime.xml).
     * Input: "2022-07-09T16:10:35"
     */
    public Map<String, String> formatDateTimePoc1() {
        Map<String, String> result = new HashMap<>();
        try {
            LocalDateTime dateTime = LocalDateTime.parse("2022-07-09T16:10:35");

            // "uuuu-MMM-dd"
            result.put("date1_Java", dateTime.format(DateTimeFormatter.ofPattern("yyyy-MMM-dd", Locale.ENGLISH)));
            // "kk:mm:ss a" (kk is 1-24 hour, use HH for 0-23 or hh for 1-12 with a)
            result.put("date2_Java", dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss a", Locale.ENGLISH)));
            // "kk:mm:ss a, MMM/dd,uuuu"
            result.put("date3_Java", dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss a, MMM/dd,yyyy", Locale.ENGLISH)));
            // Custom format "uuuu/MM/dd" (from POC2)
            result.put("dateCustom_Java", dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));

        } catch (DateTimeParseException e) {
            result.put("error", "Failed to parse date: " + e.getMessage());
        }
        return result;
    }

    /**
     * Mimics DataWeave's duration coercion and extraction (from DateTime.xml, "Duration Coercion").
     * Input: "2022-09-07T17:15:12.234" and "2021-07-10T08:14:59.899"
     */
    public Map<String, Object> calculateTimeDifferenceAndUnits(String dtStr1, String dtStr2) {
        Map<String, Object> result = new HashMap<>();
        try {
            LocalDateTime dateTime1 = LocalDateTime.parse(dtStr1);
            LocalDateTime dateTime2 = LocalDateTime.parse(dtStr2);

            Duration duration = Duration.between(dateTime2, dateTime1); // dt1 - dt2

            result.put("subtractedValue_Java_ISO8601", duration.toString()); // e.g., PT...
            result.put("extractNano_Java", duration.toNanos());
            result.put("extractMilliseconds_Java", duration.toMillis());
            result.put("extractSeconds_Java", duration.toSeconds());
            result.put("extractHours_Java", duration.toHours());
            result.put("extractDays_Java", duration.toDays()); // Added for completeness

            // Mimicking Period parsing and conversion (DW: |P1Y11M| as Number {unit: "years"})
            // Java's Period doesn't directly convert "P1Y11M" to 1.0 years if unit is "years".
            // It would give 1 year and 11 months separately.
            // For simplicity, we'll show component extraction.
            Period period1Y11M = Period.parse("P1Y11M");
            result.put("period_P1Y11M_years_Java", period1Y11M.getYears()); // 1
            result.put("period_P1Y11M_months_Java", period1Y11M.getMonths()); // 11
            
            Period period1Y12M = Period.parse("P1Y12M").normalized(); // P2Y
            result.put("period_P1Y12M_normalized_totalMonths_Java", period1Y12M.toTotalMonths()); // 24

        } catch (DateTimeParseException e) {
            result.put("error", "Failed to parse date: " + e.getMessage());
        }
        return result;
    }
    
    /**
     * Mimics DataWeave's date/time arithmetic (from DateTime.xml, "PerformOperationInDT").
     */
    public Map<String, String> dateTimeArithmetic() {
        Map<String, String> result = new HashMap<>();
        try {
            ZonedDateTime zdt = ZonedDateTime.parse("2022-07-09T17:23:21Z");
            LocalDateTime ldt = LocalDateTime.parse("2022-07-09T17:23:21");
            LocalTime lt = LocalTime.parse("17:23:21");
            DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
            DateTimeFormatter ldtFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            result.put("datePeriodSubDays_Java", zdt.minus(Period.ofDays(1)).format(formatter));
            result.put("datePeriodSubYear_Java", zdt.minus(Period.ofYears(1)).format(formatter));
            result.put("datePeriodSubMonth_Java", zdt.minus(Period.ofMonths(1)).format(formatter));
            result.put("addYear_Java", zdt.plus(Period.ofYears(1)).format(formatter));
            result.put("removeZ_Java_LDT_minus_Year", ldt.minus(Period.ofYears(1)).format(ldtFormatter)); // Using LDT for "removeZ" example

            LocalDateTime ldt2 = LocalDateTime.parse("2021-06-10T14:23:21");
            Duration diff = Duration.between(ldt2, ldt);
            result.put("AddSub_Duration_Java", diff.toString()); // Duration between two LDTs

            result.put("calculateHour_Java", lt.minus(Duration.ofHours(2)).toString());
            // DW: |PT2M| - |17:23:21| is unusual; Java LocalTime minus Duration is more standard.
            // For |PT2M| - |17:23:21|, if it means duration from time, it's negative.
            // Let's assume it's time.minus(duration) for consistency or duration.minus(time) if that's the intent.
            // Here, interpreting as time.minus(duration_from_another_time_implicit_midnight):
            Duration dFromTime = Duration.between(LocalTime.MIDNIGHT, lt); // Duration of 17:23:21 from midnight
            result.put("calculateMinutes_Java_Interpretation", Duration.ofMinutes(2).minus(dFromTime).toString());


        } catch (DateTimeParseException e) {
            result.put("error", "Failed to parse date/time: " + e.getMessage());
        }
        return result;
    }

    // --- New methods based on arraymodules.xml ---

    /**
     * Mimics DataWeave's countBy (e.g., count even/odd).
     * DW: [1,2,3,4,5,6] countBy (($ % 2) == 0) -> {false: 3, true: 3}
     */
    public Map<Boolean, Long> countByEvenOddInList(List<Integer> numbers) {
        if (numbers == null) {
            return new HashMap<>();
        }
        return numbers.stream()
                      .collect(Collectors.groupingBy(n -> (n % 2) == 0, Collectors.counting()));
    }

    /**
     * Mimics DataWeave's divideBy.
     * DW: [1,2,3,4,5,6] divideBy 2 -> [[1,2],[3,4],[5,6]]
     */
    public List<List<Integer>> divideListIntoChunks(List<Integer> numbers, int chunkSize) {
        if (numbers == null || numbers.isEmpty() || chunkSize <= 0) {
            return new ArrayList<>();
        }
        return IntStream.range(0, (numbers.size() + chunkSize - 1) / chunkSize)
                .mapToObj(i -> numbers.subList(i * chunkSize, Math.min(numbers.size(), (i + 1) * chunkSize)))
                .collect(Collectors.toList());
    }
    
    /**
     * Mimics DataWeave's sumBy.
     * DW: [{"item1": 200}, {"item1": 400}] sumBy $.item1 -> 600
     */
    public long sumByProperty(List<Map<String, Integer>> items, String key) {
        if (items == null || key == null) {
            return 0L;
        }
        return items.stream()
                    .mapToInt(map -> map.getOrDefault(key, 0))
                    .sum();
    }

    // --- New methods based on basicDw.xml and basicMathOperations.xml ---

    /**
     * Mimics variable declaration and concatenation from basicDw.xml, "declareVariables".
     * DW: var myName = "It is mulesoft"; var createObject = {...}; { key1: createObject.object1 ++ " " ++ createObject.object3 }
     */
    public Map<String, String> declareVarsAndConcat() {
        String myName = "It is mulesoft";
        Map<String, String> createObject = new HashMap<>();
        createObject.put("object1", "Mule");
        createObject.put("object2", "Soft"); // Not used in final DW output
        createObject.put("object3", myName);

        Map<String, String> result = new HashMap<>();
        result.put("key1_Java", createObject.get("object1") + " " + createObject.get("object3"));
        return result;
    }

    /**
     * Mimics type coercion and formatting from basicDw.xml, "POC1" (Coercion).
     */
    public Map<String, Object> typeCoercionPoc1() {
        Map<String, Object> result = new HashMap<>();
        try {
            // "key1": "22" as Number as String {format: ".00"} as Number -> 22.0
            double k1_num = Double.parseDouble("22");
            String k1_str_formatted = String.format(Locale.ENGLISH, "%.2f", k1_num); // "22.00"
            result.put("key1_Java", Double.parseDouble(k1_str_formatted)); // 22.0

            // "key2": 125748.32596 as String {format: "#.###"} as Number -> 125748.326
            java.text.DecimalFormat df = new java.text.DecimalFormat("#.###");
            df.setRoundingMode(java.math.RoundingMode.HALF_UP);
            String k2_str_formatted = df.format(125748.32596);
            result.put("key2_Java", Double.parseDouble(k2_str_formatted.replace(",", ""))); // Ensure no locale-specific grouping chars

            // "key3": "2020-07-09" as Date as String {format: "dd-MMM-yy", locale: "in"} -> "09-Jul-20"
            // For "in" locale, need to map to a Java Locale. "en_IN" is a common choice for Indian English.
            LocalDate k3_date = LocalDate.parse("2020-07-09");
            // Note: "MMM" is locale-specific.
            result.put("key3_Java", k3_date.format(DateTimeFormatter.ofPattern("dd-MMM-yy", new Locale("en", "IN"))));

        } catch (NumberFormatException | DateTimeParseException e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * Mimics basic arithmetic and object key removal from basicMathOperations.xml, "BasicMathOperations".
     * Interpreting "arrayAdd": [1,2,3] -1 +4 as element-wise operation: (x-1)+4 = x+3.
     */
    public Map<String, Object> basicArithmeticAndObjectOps() {
        Map<String, Object> n1 = new HashMap<>();
        n1.put("add_Java", 2 + 2);
        n1.put("sub_Java", 2 - 2);
        n1.put("mul_Java", 2 * 2);
        n1.put("div_Java", 45.0 / 98.0); // Ensure floating point division

        List<Integer> initialArray = Arrays.asList(1, 2, 3);
        List<Integer> arrayAddResult = initialArray.stream()
                                                 .map(x -> x - 1 + 4) // x + 3
                                                 .collect(Collectors.toList());
        n1.put("arrayAdd_Java_Interpreted", arrayAddResult);

        Map<String, Integer> objectMap = new HashMap<>();
        objectMap.put("key1", 1);
        objectMap.put("key2", 2);
        objectMap.put("key3", 3);
        objectMap.remove("key1"); // Mimics { ... } - "key1"
        n1.put("objectAdd_Java", objectMap);
        
        // The DW output was application/xml with n1 as root.
        // For Java, we'll return the inner map. Camel can marshal this to XML if needed.
        return n1; 
    }

    /**
     * Mimics equality and relational operators from basicMathOperations.xml, "Equality_Realtional_Operator".
     */
    public Map<String, Boolean> equalityRelationalCheck() {
        Map<String, Boolean> result = new HashMap<>();
        result.put("k1_Java", 1 < 1);
        result.put("k2_Java", 1 <= -8);
        result.put("k3_Java", 56 > 98);
        result.put("k4_Java", 876 >= 876.0);
        result.put("k5_Java", 1 == 1);
        result.put("k6_Java", "manju".equals("manju")); // String comparison
        try {
            // DW "~=" (type-coercing equality)
            result.put("k7_Java", Integer.parseInt("45") == 45);
        } catch (NumberFormatException e) {
            result.put("k7_Java_Error", false); // Or handle error appropriately
        }
        return result;
    }

    // --- New methods based on dwTypes.xml and functions.xml ---

    /**
     * Mimics basic type demonstrations from dwTypes.xml, "basicTypes" transform.
     */
    public Map<String, Object> demonstrateBasicTypesMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("stringValue_Java", "this is string");
        
        Map<String, Number> numberValue = new HashMap<>();
        numberValue.put("key1", 1);
        numberValue.put("key2", 23.45);
        result.put("numberValue_Java", numberValue);

        Map<String, Boolean> booleanValue = new HashMap<>();
        booleanValue.put("key1", true);
        booleanValue.put("key2", false);
        result.put("thisisboolean_Java", booleanValue);

        List<Map<String, Integer>> thisIsArray = new ArrayList<>();
        thisIsArray.add(Map.of("key1", 1));
        result.put("thisisArray_Java", thisIsArray);

        Map<String, List<Object>> thisIsMyObject = new HashMap<>();
        thisIsMyObject.put("key1", new ArrayList<>());
        result.put("thisisMyObject_Java", thisIsMyObject);
        
        List<Object> anotherExampleOfArray = Arrays.asList(
            1, 2, 3, "manjukaushik", true, Map.of("key2", "mulesoft")
        );
        result.put("anotherexampleofArray_Java", anotherExampleOfArray);
        return result;
    }

    /**
     * Mimics DataWeave's typeOf operator for a few examples.
     * DW: typeOf("manju") -> "String", typeOf(10) -> "Number"
     */
    public Map<String, String> getTypeOfStringAndNumber() {
        Map<String, String> result = new HashMap<>();
        result.put("typeOfString_Java", "Manju".getClass().getSimpleName()); // "String"
        result.put("typeOfInteger_Java", ((Object)10).getClass().getSimpleName()); // "Integer"
        result.put("typeOfDouble_Java", ((Object)10.5).getClass().getSimpleName()); // "Double"
        result.put("typeOfEmptyString_Java", "".getClass().getSimpleName()); // "String"
        return result;
    }

    /**
     * Mimics list manipulation from dwTypes.xml, "Prepend_Append_Remove_Operator".
     * Handles interpreted operations.
     */
    public Map<String, List<Object>> listManipulationOperations() {
        Map<String, List<Object>> result = new HashMap<>();

        // "key1": 76 >> [109] -> [76, 109]
        List<Object> key1List = new ArrayList<>(Arrays.asList(109));
        key1List.add(0, 76);
        result.put("key1_Java", key1List);

        // "key2": [151] << 109 -> [151, 109]
        List<Object> key2List = new ArrayList<>(Arrays.asList(151));
        key2List.add(109);
        result.put("key2_Java", key2List);
        
        // "key3": "manju" >> ["kaushik"] -> ["manju", "kaushik"]
        List<Object> key3List = new ArrayList<>(Arrays.asList("kaushik"));
        key3List.add(0, "manju");
        result.put("key3_Java", key3List);

        // "key4": [154] + 165 -> Interpreted as [154, 165] (concatenation)
        List<Object> key4List = new ArrayList<>(Arrays.asList(154));
        key4List.add(165);
        result.put("key4_Java", key4List);
        
        // "key5": [154,187,234] - 187 -> [154, 234]
        List<Object> key5List = new ArrayList<>(Arrays.asList(154, 187, 234));
        key5List.remove((Object)187); // Remove first occurrence of value
        result.put("key5_Java", key5List);

        // "key6": ["manju","kaushik","location"] - "location" - "manju" - "kaushik" -> []
        List<Object> key6List = new ArrayList<>(Arrays.asList("manju", "kaushik", "location"));
        key6List.remove("location");
        key6List.remove("manju");
        key6List.remove("kaushik");
        result.put("key6_Java", key6List);

        // "key7": ([{a:b}, {c:d}, {e:f}] - {c:d}) -> [{a:b}, {e:f}]
        List<Object> key7List = new ArrayList<>(Arrays.asList(
            Map.of("a", "b"), Map.of("c", "d"), Map.of("e", "f")
        ));
        // Java list removal of Map requires exact object or custom predicate.
        // For Maps, .equals() works if keys/values are same.
        key7List.remove(Map.of("c", "d"));
        result.put("key7_Java", key7List);
        
        return result;
    }

    // --- Methods from functions.xml ---

    public Map<String, Object> moduleAgeID(String name, String location, int id, int age, int salary) {
        Map<String, Object> result = new HashMap<>();
        Object k1Value;
        if (age > 60 && id >= 1) {
            k1Value = name;
        } else if (age < 60 && id >= 1) {
            k1Value = location;
        } else {
            k1Value = salary;
        }
        result.put("k1", k1Value);
        return result;
    }
    
    // DW: fun emptyFunction() = [ k1: "hello" ] -> Array of one object
    public List<Map<String, String>> emptyFunction() {
        return Collections.singletonList(Map.of("k1", "hello"));
    }

    // Wrapper to call functions as in "createFunction" transform
    public Map<String, Object> invokeCustomFunctions() {
        Map<String, Object> result = new HashMap<>();
        result.put("output1_Java", moduleAgeID("Manju", "india", 3, 45, 100));
        result.put("output2_Java", moduleAgeID("rahul", "usa", 1, 74, 100));
        // Assuming moduleAgeSalary would be similar to moduleAgeID for this example
        // result.put("output3_Java", moduleAgeSalary("rahul", "usa", 1, 74, 100));
        result.put("output4_Java", emptyFunction());
        return result;
    }

    // Mimics functionParam1(p1=6, p2=2, p3=3) from "precedenceOfOptionalParam"
    // Java handles optional params via overloading or by passing nulls and handling defaults internally.
    public Map<String, Integer> functionWithOptionalParams(Integer p1, Integer p2, Integer p3) {
        Map<String, Integer> result = new HashMap<>();
        result.put("k1", (p1 != null) ? p1 : 6);
        result.put("k2", (p2 != null) ? p2 : 2);
        result.put("k3", (p3 != null) ? p3 : 3);
        return result;
    }
    
    // Wrapper to demonstrate precedence from "precedenceOfOptionalParam"
    public Map<String, Object> invokeFunctionWithOptionalParams() {
        Map<String, Object> result = new HashMap<>();
        result.put("k1_Java", functionWithOptionalParams(10, null, null)); // p1=10, p2=2 (default), p3=3 (default)
        result.put("k2_Java", functionWithOptionalParams(10, 20, null));   // p1=10, p2=20, p3=3 (default)
        result.put("k3_Java", functionWithOptionalParams(10, 20, 40));  // p1=10, p2=20, p3=40
        return result;
    }

    // --- New methods based on mapAndFilter.xml and pluckReduce.xml ---

    /**
     * Mimics DataWeave's map operation on a list of maps.
     * DW example: studentArrayList map (item, index) -> { "studentID": item.studentID ++ "_" ++ item.studentName, "stduentLocation": item.studentLocation }
     */
    public List<Map<String, Object>> mapStudentData(List<Map<String, Object>> studentList) {
        if (studentList == null) {
            return new ArrayList<>();
        }
        return studentList.stream()
            .map(item -> {
                Map<String, Object> newItem = new HashMap<>();
                newItem.put("studentID_Java", item.get("studentID") + "_" + item.get("studentName"));
                newItem.put("studentLocation_Java", item.get("studentLocation"));
                return newItem;
            })
            .collect(Collectors.toList());
    }

    /**
     * Mimics DataWeave's map operation on a list of strings to an object.
     * DW example: [ "manju", "mohan", "rajeev" ] map ($$+2): $ ++ 1
     * Java: Will create a Map where keys are (index + offset) and values are (string + suffix).
     */
    public Map<String, String> mapStringsToIndexedObject(List<String> names, int indexOffset, String valueSuffix) {
        Map<String, String> result = new HashMap<>();
        if (names == null) {
            return result;
        }
        for (int i = 0; i < names.size(); i++) {
            result.put(String.valueOf(i + indexOffset), names.get(i) + valueSuffix);
        }
        return result;
    }
    
    /**
     * Mimics DataWeave's filterObject operation.
     * DW example: studentArrayList[0] filterObject ( (value, key) -> (value startsWith ("M")) and (key startsWith ("s")) )
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> filterObjectByKeyValue(Map<String, Object> inputObject, String valuePrefix, String keyPrefix) {
        if (inputObject == null) {
            return new HashMap<>();
        }
        return inputObject.entrySet().stream()
            .filter(entry -> {
                boolean keyMatches = entry.getKey().startsWith(keyPrefix);
                boolean valueMatches = false;
                if (entry.getValue() instanceof String) {
                    valueMatches = ((String) entry.getValue()).startsWith(valuePrefix);
                }
                // Add more type checks for value if needed
                return keyMatches && valueMatches;
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Mimics DataWeave's pluck operation on an array of objects (List of Maps).
     * DW example: array pluck $.key
     */
    public <T> List<T> pluckValueFromListOfMaps(List<Map<String, T>> list, String key) {
        if (list == null || key == null) {
            return new ArrayList<>();
        }
        return list.stream()
            .map(map -> map.get(key))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Mimics DataWeave's reduce operation for summing numbers.
     * DW example: [10,20,30] reduce (item, acc = 0) -> acc + item
     */
    public Integer reduceToSum(List<Integer> numbers, Integer initialAccumulator) {
        if (numbers == null) {
            return initialAccumulator == null ? 0 : initialAccumulator;
        }
        return numbers.stream().reduce(initialAccumulator == null ? 0 : initialAccumulator, Integer::sum);
    }

    /**
     * Mimics DataWeave's reduce operation for string concatenation.
     * DW example: [1,2,3,4,5,89] reduce ($$ ++ $) -> "1234589" (numbers become strings)
     */
    public String reduceToStringConcat(List<?> items) {
        if (items == null) {
            return "";
        }
        return items.stream()
                    .map(String::valueOf) // Convert each item to string
                    .reduce("", (acc, itemStr) -> acc + itemStr); // Concatenate
    }

    // --- New methods for specific operators from 1operators.xml ---

    /**
     * Calculates the average of a list of numbers.
     * Mimics DataWeave's avg() function.
     * DW: avg([1,1000]) -> 500.5
     */
    public Double avg(List<? extends Number> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return 0.0; // Or throw IllegalArgumentException("Input list cannot be null or empty.");
        }
        return numbers.stream()
                      .mapToDouble(Number::doubleValue)
                      .average()
                      .orElse(0.0); // Should not happen if not empty, but average() returns OptionalDouble
    }

    /**
     * Returns a list with distinct elements based on their natural equality.
     * Mimics DataWeave's distinctBy $
     * DW: [1,2,3,1,2,4] distinctBy $ -> [1,2,3,4]
     */
    public <T> List<T> distinctByValue(List<T> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Returns a list of maps, keeping only the first map for each distinct value of the specified key.
     * Mimics DataWeave's distinctBy $.key
     * DW: [{id:1, v:10}, {id:2, v:20}, {id:1, v:30}] distinctBy $.id -> [{id:1, v:10}, {id:2, v:20}]
     */
    public List<Map<String, Object>> distinctByKey(List<Map<String, Object>> list, String key) {
        if (list == null || key == null) {
            return new ArrayList<>();
        }
        // Keep track of seen keys to ensure only the first occurrence is kept
        List<Object> seenKeys = new ArrayList<>();
        return list.stream()
                   .filter(map -> {
                       Object keyValue = map.get(key);
                       if (!seenKeys.contains(keyValue)) {
                           seenKeys.add(keyValue);
                           return true;
                       }
                       return false;
                   })
                   .collect(Collectors.toList());
    }

    /**
     * Checks if mainString ends with suffix.
     * Mimics DataWeave's endsWith operator.
     * DW: "mulesofts" endsWith "fts" -> true
     */
    public boolean checkEndsWith(String mainString, String suffix) {
        if (mainString == null || suffix == null) {
            return false;
        }
        return mainString.endsWith(suffix);
    }

    /**
     * Returns the first index of element in list, or -1 if not found.
     * Mimics DataWeave's indexOf(array, value)
     * DW: ["a", "b", "c"] indexOf "b" -> 1
     */
    public int findFirstIndexOfInList(List<?> list, Object element) {
        if (list == null) {
            return -1;
        }
        return list.indexOf(element);
    }

    /**
     * Returns the first index of subString in mainString, or -1 if not found.
     * Mimics DataWeave's indexOf(string, substring)
     * DW: "hello world" indexOf "world" -> 6
     */
    public int findFirstIndexOfInString(String mainString, String subString) {
        if (mainString == null || subString == null) {
            return -1;
        }
        return mainString.indexOf(subString);
    }

    /**
     * Finds the minimum value in a list of numbers.
     * Mimics DataWeave's min() function.
     * DW: min([1,1000, 0.2]) -> 0.2
     */
    public <N extends Number & Comparable<N>> N minInList(List<N> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return null; // Or throw, or return a specific N type like Double.NaN
        }
        return numbers.stream().min(Comparable::compareTo).orElse(null);
    }

    /**
     * Finds the maximum value in a list of numbers.
     * Mimics DataWeave's max() function.
     * DW: max([1,1000, 0.2]) -> 1000
     */
    public <N extends Number & Comparable<N>> N maxInList(List<N> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return null;
        }
        return numbers.stream().max(Comparable::compareTo).orElse(null);
    }
}
