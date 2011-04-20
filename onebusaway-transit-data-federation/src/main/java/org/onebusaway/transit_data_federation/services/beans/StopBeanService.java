package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.transit_data.model.StopBean;

public interface StopBeanService {

  /**
   * @param stopId see {@link Stop#getId()}
   * @return the populated stop bean, or null if a stop with the specified id
   *         was not found
   * @throws NoSuchStopServiceException if the stop with the specified id could
   *           not be found
   */
  public StopBean getStopForId(AgencyAndId stopId);
}
