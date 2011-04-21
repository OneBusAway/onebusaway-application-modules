package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.TransitLocationBean;
import org.onebusaway.transit_data.model.tripplanning.ItinerariesBean;
import org.onebusaway.transit_data.model.tripplanning.TransitShedConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.VertexBean;
import org.onebusaway.transit_data_federation.model.TargetTime;

public interface ItinerariesBeanService {

  /**
   * 
   * @param from
   * @param to
   * @param targetTime TODO
   * @param constraints
   * @return a list of trip plans computed between the two locations with the
   *         specified constraints
   * @throws ServiceException
   */
  public ItinerariesBean getItinerariesBetween(TransitLocationBean from,
      TransitLocationBean to, TargetTime targetTime, ConstraintsBean constraints)
      throws ServiceException;

  public ListBean<VertexBean> getStreetGraphForRegion(double latFrom,
      double lonFrom, double latTo, double lonTo);

  public MinTravelTimeToStopsBean getMinTravelTimeToStopsFrom(
      CoordinatePoint location, long time,
      TransitShedConstraintsBean constraints);
}
