/**
 * Copyright (C) 2021 CaylaSavitzky <CaylaSavitzky@gmail.com>
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
package org.onebusaway.api.model.transit;

import java.io.Serializable;
import java.util.List;


/**
 * Support Schedule queries at the route level.  Similar to
 * StopScheduleBeanServiceImpl.
 *
 *   Ultimate goal to deliver data in this format:
 *     "entry": {
 *   "routeId": "40_100479",
 *   "serviceIds": ["SERVICEIDVALUE1","SERVICEIDVALUE2"],
 *   "scheduleDate": 1609315200,
 *   "stopTripGroupings": [
 *     {
 *       "directionId": 0,
 *       "tripHeadsign": "University of Washington Station",
 *       "stopIds": ["STOPID1", "STOPID2"],
 *       "tripIds": ["TRIPID1", "TRIPID2"]
 *     },
 *     {
 *       "directionId": 1,
 *       "tripHeadsign": "Angle Lake Station",
 *       "stopIds": ["STOPID2", "STOPID3"],
 *       "tripIds": ["TRIPID3", "TRIPID4"]
 *     }
 *   ]
 * }
 */
public class RouteScheduleV2Bean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String routeId;
    private List<String> serviceIds;
    private long scheduleDate;
    private List<StopsAndTripsForDirectionV2Bean> stopTripGroupings;

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public List<String> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(List<String> serviceIds) {
        this.serviceIds = serviceIds;
    }

    public long getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(long scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public List<StopsAndTripsForDirectionV2Bean> getStopTripGroupings() {
        return stopTripGroupings;
    }

    public void setStopTripGroupings(List<StopsAndTripsForDirectionV2Bean> stopTripGroupings) {
        this.stopTripGroupings = stopTripGroupings;
    }
}
