package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.RegisterAlarmQueryBean;

public interface ArrivalAndDepartureAlarmService {

  public AgencyAndId registerAlarmForArrivalAndDepartureAtStop(
      ArrivalAndDepartureQuery query, RegisterAlarmQueryBean alarmBean);

  public void cancelAlarmForArrivalAndDepartureAtStop(AgencyAndId alarmId);
}
