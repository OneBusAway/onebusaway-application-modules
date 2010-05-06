package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopBean;

public interface StopBeanService {

  /**
   * 
   * @param id
   * @return the populated stop bean, or null if a stop with the specified id
   *         was not found
   */
  public StopBean getStopForId(AgencyAndId id);
}
