package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopBean;

import java.util.List;

public interface NearbyStopsBeanService {
  public List<AgencyAndId> getNearbyStops(StopBean stopBean, double radius);
}
