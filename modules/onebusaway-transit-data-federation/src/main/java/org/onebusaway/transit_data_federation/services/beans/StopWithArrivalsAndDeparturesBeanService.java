package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;

import java.util.Date;
import java.util.Set;

public interface StopWithArrivalsAndDeparturesBeanService {
  public StopWithArrivalsAndDeparturesBean getArrivalsAndDeparturesByStopId(AgencyAndId id, Date time);

  public StopsWithArrivalsAndDeparturesBean getArrivalsAndDeparturesForStopIds(
      Set<AgencyAndId> ids, Date time);
}
