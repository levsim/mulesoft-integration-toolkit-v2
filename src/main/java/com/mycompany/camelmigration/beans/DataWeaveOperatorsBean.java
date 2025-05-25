package com.mycompany.camelmigration.beans;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;

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
}
