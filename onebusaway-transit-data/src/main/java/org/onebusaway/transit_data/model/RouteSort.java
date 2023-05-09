package org.onebusaway.transit_data.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RouteSort implements Serializable {

    private final Map<String, String> agencySortConfiguration;

    private final Map<String, Map<String, Integer>> routeSortOrderMap = new HashMap<>();

    private final String delimiter;

    RouteSort(Map<String, String> agencySortConfiguration) {
        this.agencySortConfiguration = agencySortConfiguration;
        this.delimiter = ",";
        setOrderingToMap();
    }

    void setOrderingToMap(){
        for (String agencyId : agencySortConfiguration.keySet()) {

            String sortOrderString = agencySortConfiguration.get(agencyId);
            String[] sortOrderItems = sortOrderString.split(delimiter);
            Map<String, Integer> sortOrderMap = new HashMap<>();

            for(int i = 0; i< sortOrderItems.length; i++){
                sortOrderMap.put(sortOrderItems[i].trim(), i);
            }

            routeSortOrderMap.put(agencyId, sortOrderMap);
        }

    }

     public int compareRoutes(String a, String b, RouteSort routeSortInstance, String agencyId) {

         if(a == null && b == null){
             return 0;
         }

         if(a == null){
             return 1;
         }

         if(b == null){
             return -1;
         }

        Map<String, Integer> sortOrderMap = routeSortInstance.routeSortOrderMap.get(agencyId);
        // standard ordering if the configuration is not provided
        if (sortOrderMap == null) {
            return a.compareTo(b);
        }

        Integer o1 = sortOrderMap.get(a);
        Integer o2 = sortOrderMap.get(b);

        // the value not found in configuration so use natural order
        if (o1 == null && o2 == null) {
            return a.compareTo(b);
        }

        if (o1 == null) {
            return 1;
        }

        if (o2 == null) {
            return -1;
        }
        // compare based on the values inside the map
        return Integer.compare(o1, o2);
    }

    public String getDelimiter() {
        return delimiter;
    }

    public Map<String, Map<String, Integer>> getRouteSortOrderMap() {
        return routeSortOrderMap;
    }
}

