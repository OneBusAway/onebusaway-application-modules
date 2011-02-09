package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlannerConstraintsBean;

import java.util.List;

/**
 * Service methods for computing trip planner itineraries.
 * 
 * @author bdferris
 * @see TripPlanBean
 */
public interface TripPlannerBeanService {

  /**
   * 
   * @param latFrom
   * @param lonFrom
   * @param latTo
   * @param lonTo
   * @param constraints
   * @return
   * @throws ServiceException
   */
  public List<TripPlanBean> getTripsBetween(double latFrom, double lonFrom,
      double latTo, double lonTo, TripPlannerConstraintsBean constraints)
      throws ServiceException;

  /**
   * 
   * @param lat
   * @param lon
   * @param constraints
   * @return
   * @throws ServiceException
   */
  public MinTravelTimeToStopsBean getMinTravelTimeToStopsFrom(double lat,
      double lon, OneBusAwayConstraintsBean constraints)
      throws ServiceException;
}
