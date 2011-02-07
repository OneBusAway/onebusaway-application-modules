package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.ItinerariesBean;
import org.onebusaway.transit_data.model.tripplanning.VertexBean;

public interface ItinerariesBeanService {

  /**
   * 
   * @param latFrom
   * @param lonFrom
   * @param latTo
   * @param lonTo
   * @param constraints
   * @return a list of trip plans computed between the two locations with the
   *         specified constraints
   * @throws ServiceException
   */
  public ItinerariesBean getItinerariesBetween(double latFrom, double lonFrom,
      double latTo, double lonTo, ConstraintsBean constraints)
      throws ServiceException;

  public ListBean<VertexBean> getStreetGraphForRegion(double latFrom,
      double lonFrom, double latTo, double lonTo);
}
