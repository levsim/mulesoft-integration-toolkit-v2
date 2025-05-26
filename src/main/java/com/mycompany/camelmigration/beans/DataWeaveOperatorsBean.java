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
}
