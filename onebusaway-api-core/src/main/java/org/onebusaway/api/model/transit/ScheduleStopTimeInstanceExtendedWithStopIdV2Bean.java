package org.onebusaway.api.model.transit;

public class ScheduleStopTimeInstanceExtendedWithStopIdV2Bean extends ScheduleStopTimeInstanceV2Bean{
    String stopId;


    public void setStopId(String stopId){
        this.stopId = stopId;
    }

    public String getStopId(){
        return stopId;
    }
}
