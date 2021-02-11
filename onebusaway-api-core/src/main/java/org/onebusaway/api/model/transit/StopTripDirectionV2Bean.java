/**
 * Copyright (C) 2020 Cambridge Systematics, Inc.
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

import org.onebusaway.api.model.transit.AgencyV2Bean;
import org.onebusaway.api.model.transit.RouteV2Bean;
import org.onebusaway.api.model.transit.StopV2Bean;
import org.onebusaway.api.model.transit.TripV2Bean;
import org.onebusaway.api.model.transit.schedule.StopTimeV2Bean;
import org.onebusaway.transit_data.model.StopTripDirectionBean;

import java.util.ArrayList;
import java.util.List;
/**
 *      {
 *      "directionId": 0,
 *      "tripHeadsign": "University of Washington Station",
 *      "stopIds": ["STOPID1", "STOPID2"],
 *      "tripIds": ["TRIPID1", "TRIPID2"]
 *      }
 **/
public class StopTripDirectionV2Bean {
    private static final long serialVersionUID = 1L;

    private String directionId;
    private String tripHeadsign;
    private List<String> stopIds = new ArrayList<>();
    private List<String> tripIds = new ArrayList<>();
    private List<List<ScheduleStopTimeInstanceV2Bean>> stopTimes = new ArrayList();


    public void setDirectionId(String directionId){
        this.directionId=directionId;
    }

    public String getDirectionId() {
        return directionId;
    }

    public void setTripHeadsign(String tripHeadsign) {
        this.tripHeadsign = tripHeadsign;
    }

    public String getTripHeadsign() {
        return tripHeadsign;
    }

    public void setStopIds(List<String> stopIds) {
        this.stopIds = stopIds;
    }

    public List<String> getStopIds() {
        return stopIds;
    }

    public List<String> getTripIds() {
        return tripIds;
    }

    public void setTripIds(List<String> tripIds) {
        this.tripIds = tripIds;
    }

    public void addTripId(String tripId){
        tripIds.add(tripId);
    }

    public void setStopTimes(List<List<ScheduleStopTimeInstanceV2Bean>> stopTimes) {
        this.stopTimes = stopTimes;
    }

    public List<List<ScheduleStopTimeInstanceV2Bean>> getStopTimes() {
        return stopTimes;
    }
}
