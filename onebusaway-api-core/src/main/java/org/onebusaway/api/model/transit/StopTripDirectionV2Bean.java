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
}
