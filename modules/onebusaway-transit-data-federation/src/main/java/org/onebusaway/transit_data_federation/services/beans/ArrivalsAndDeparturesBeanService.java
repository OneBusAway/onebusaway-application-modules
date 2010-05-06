package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;

import java.util.Date;
import java.util.List;

public interface ArrivalsAndDeparturesBeanService {
  public List<ArrivalAndDepartureBean> getArrivalsAndDeparturesByStopId(AgencyAndId id, Date time);
}
