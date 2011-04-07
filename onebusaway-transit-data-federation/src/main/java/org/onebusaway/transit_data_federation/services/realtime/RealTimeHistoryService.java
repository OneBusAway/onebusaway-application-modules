package org.onebusaway.transit_data_federation.services.realtime;


public interface RealTimeHistoryService {
  public ScheduleDeviationHistogram getScheduleDeviationHistogramForArrivalAndDepartureInstance(
      ArrivalAndDepartureInstance instance, int stepSizeInSeconds); 
}
