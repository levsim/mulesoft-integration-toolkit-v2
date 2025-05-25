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
            
            .setBody(simple("Successfully tested DataWeave operator bean methods. Final JSON body is from createSampleMap."));
    }
}
