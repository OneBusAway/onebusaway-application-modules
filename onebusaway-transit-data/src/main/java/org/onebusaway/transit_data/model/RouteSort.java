package org.onebusaway.transit_data.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RouteSort implements Serializable {

    private final Map<String, Integer> sortOrderMap = new HashMap<>();

    private final String sortOrderString;

    private final String delimiter;

    RouteSort(String sortOrderString) {
        this.sortOrderString = sortOrderString;
        this.delimiter = ",";
        setOrderingToMap();
    }

    RouteSort(String sortOrderString, String delimiter){
        this.sortOrderString = sortOrderString;
        this.delimiter = delimiter;
        setOrderingToMap();
    }

    void setOrderingToMap(){
        String[] sortOrderItems = sortOrderString.split(delimiter);

        for(int i = 0; i< sortOrderItems.length; i++){
            sortOrderMap.put(sortOrderItems[i].trim(), i);
        }
    }

    static public int compareRoutes(String a, String b, RouteSort routeSortInstance) {

        Integer o1 = routeSortInstance.getSortOrderMap().get(a);
        Integer o2 = routeSortInstance.getSortOrderMap().get(b);

        if(a == null && b == null){
            return 0;
        }

        if(a == null){
            return 1;
        }

        if(b == null){
            return -1;
        }

        if (o1 == null && o2 == null) {
            return a.compareTo(b);
        }

        if (o1 == null) {
            return 1;
        }

        if (o2 == null) {
            return -1;
        }

        return Integer.compare(o1, o2);
    }

    public Map<String, Integer> getSortOrderMap() {
        return sortOrderMap;
    }

    public String getSortOrderString() {
        return sortOrderString;
    }

    public String getDelimiter() {
        return delimiter;
    }

}

