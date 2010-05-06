package org.onebusaway.transit_data_federation.services.beans;

import java.util.Date;
import java.util.Set;

import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;

public interface StopWithArrivalsAndDeparturesBeanService {
  public StopWithArrivalsAndDeparturesBean getArrivalsAndDeparturesByStopId(AgencyAndId id, Date timeFrom, Date timeTo);

  public StopsWithArrivalsAndDeparturesBean getArrivalsAndDeparturesForStopIds(
      Set<AgencyAndId> ids, Date timeFrom, Date timeTo) throws NoSuchStopServiceException;
}
