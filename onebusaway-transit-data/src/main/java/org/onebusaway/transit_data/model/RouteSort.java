/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RouteSort implements RouteSorting, Serializable {

    private final Map<String, String> agencySortConfiguration;

    private String primarySortAgency;
    private final Map<String, Map<String, Integer>> routeSortOrderMap = new HashMap<>();

    private final String delimiter;

    RouteSort(Map<String, String> agencySortConfiguration, String primarySortAgency) {
        this.agencySortConfiguration = agencySortConfiguration;
        this.delimiter = ",";
        this.primarySortAgency = primarySortAgency;
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

     public int compareRoutes(String a, String b) {

         if(a == null && b == null){
             return 0;
         }

         if(a == null){
             return 1;
         }

         if(b == null){
             return -1;
         }

        Map<String, Integer> sortOrderMap = this.routeSortOrderMap.get(primarySortAgency);
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

    public String getPrimarySortAgency() {
        return primarySortAgency;
    }
}

