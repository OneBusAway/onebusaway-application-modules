package org.onebusaway.transit_data.model;

import org.onebusaway.gtfs.model.AgencyAndId;


public class StopTimeInstanceBeanExtendedWithStopId extends StopTimeInstanceBean{
    AgencyAndId stopId;

    public void setStopId(AgencyAndId stopId){
        this.stopId = stopId;
    }

    public AgencyAndId getStopId(){
        return stopId;
    }
}
