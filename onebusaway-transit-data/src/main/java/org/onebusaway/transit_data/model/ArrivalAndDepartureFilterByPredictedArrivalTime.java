package org.onebusaway.transit_data.model;

public class ArrivalAndDepartureFilterByPredictedArrivalTime extends ArrivalAndDepartureFilter{

    @Override
    public boolean matches(ArrivalAndDepartureBean bean) {
        if(!( bean.getPredictedArrivalTime() > 0 && bean.getPredictedArrivalTime() < System.currentTimeMillis())){
            return true;
        }
        return false;
    }

}
