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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *      {
 *      "directionId": 0,
 *      "tripHeadsign": "University of Washington Station",
 *      "stopIds": ["STOPID1", "STOPID2"],
 *      "tripIds": ["TRIPID1", "TRIPID2"]
 *      }
 **/
public class StopsAndTripsForDirectionV2Bean {
    private static final long serialVersionUID = 1L;

    private String directionId;
    private Set<String> tripHeadsigns;
    private List<String> stopIds = new ArrayList<>();
    private List<String> tripIds = new ArrayList<>();
    private List<TripWithStopTimesV2Bean> tripsWithStopTimes = new ArrayList();


    public void setDirectionId(String directionId){
        this.directionId=directionId;
    }

    public String getDirectionId() {
        return directionId;
    }

    public void setTripHeadsigns(Set<String> tripHeadsigns) {
        this.tripHeadsigns = tripHeadsigns;
    }

    public Set<String> getTripHeadsigns() {
        return tripHeadsigns;
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

    public void setTripsWithStopTimes(List<TripWithStopTimesV2Bean> tripsWithStopTimes) {
        this.tripsWithStopTimes = tripsWithStopTimes;
    }

    public List<TripWithStopTimesV2Bean> getTripsWithStopTimes() {
        return tripsWithStopTimes;
    }
}
