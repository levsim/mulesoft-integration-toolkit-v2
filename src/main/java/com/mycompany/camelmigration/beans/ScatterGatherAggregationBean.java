package com.mycompany.camelmigration.beans;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Component("scatterGatherAggregationBean")
public class ScatterGatherAggregationBean {

    /**
     * Aggregates payloads from a list of Exchanges.
     * This method assumes each exchange in the list has a body that is a Map,
     * and this map contains a key "payload" whose value is to be extracted.
     * This is a common pattern but might need adjustment based on the actual
     * output of the scatter-gather routes.
     * The original DataWeave `payload..payload` is very generic. This implementation
     * makes a reasonable assumption.
     *
     * @param exchanges A list of Exchange objects from the Scatter-Gather EIP.
     * @return A list of extracted "payload" objects.
     */
    @SuppressWarnings("unchecked") // For casting Map value
    public List<Object> aggregateFromExchanges(List<Exchange> exchanges) {
        if (exchanges == null) {
            return new ArrayList<>();
        }
        return exchanges.stream()
            .map(exchange -> {
                Object body = exchange.getIn().getBody();
                if (body instanceof Map) {
                    // Assuming the structure from Mule's set-payload in scatter-gather routes
                    // e.g. { "payload01Name": "Manju", "payload01ID": 1 ... }
                    // The DW script `payload..payload` would collect these entire maps if they are the direct output of routes.
                    // If the routes *contain* a field named "payload", then .get("payload") is correct.
                    // Given the DW `payload..payload`, it's more likely it expects to find a key literally named "payload".
                    // Let's assume the individual routes in scatter-gather produce a Map, and that Map *is* the payload to be collected.
                    // Or, if each route's output is a Map that *contains* a "payload" field:
                    // return ((Map<String, Object>) body).get("payload");
                    
                    // For the Mule example:
                    // <set-payload value='#[{"payload01Name": "Manju", ...}]' />
                    // The result of this is the Map itself. So, `payload..payload` when applied to an aggregation
                    // of these maps (e.g. {0: {payload01Name...}, 1: {payload02Name...}}) would collect these inner maps.
                    // If the default Camel aggregation strategy for multicast results in List<Object> where Object is the body of each route,
                    // then this is correct.
                    return body; 
                }
                return null; 
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Alternative aggregation if the input is already a List of Maps (bodies of exchanges).
     * This is often the case if a simple CollectionAggregationStrategy is used.
     */
    @SuppressWarnings("unchecked")
    public List<Object> aggregateFromListOfMaps(List<Map<String, Object>> bodies) {
        if (bodies == null) {
            return new ArrayList<>();
        }
        // If `payload..payload` means collecting the maps themselves:
        return new ArrayList<>(bodies);
        // If it means collecting values of a key named "payload" from within each map:
        // return bodies.stream()
        //     .map(map -> map.get("payload"))
        //     .filter(Objects::nonNull)
        //     .collect(Collectors.toList());
    }
}
