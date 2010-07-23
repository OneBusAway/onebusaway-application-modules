package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.transit_data.model.AgencyBean;

/**
 * Service for querying {@link AgencyBean} representations of {@link Agency}
 * objects
 * 
 * @author bdferris
 * @see AgencyBean
 * @see Agency
 */
public interface AgencyBeanService {

  /**
   * @param agencyId see {@lnk Agency#getId()}
   * @return a bean representation of the {@link Agency} with the specified id
   */
  public AgencyBean getAgencyForId(String agencyId);
}
