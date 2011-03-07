package org.onebusaway.transit_data_federation.services.oba;

import java.util.List;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;

/**
 * Service methods for the "one bus ride away" transit-shed calculation aka what
 * are all the places we can get to from a certain starting point in a
 * particular amount of time.
 * 
 * @author bdferris
 * 
 */
public interface OneBusAwayService {

  /**
   * Given a set of stops with min-travel time already determined, and a set of
   * nearby places, complete the best plan calculation for each by looking for
   * valid walking paths from stops to places and keeping the shortest paths.
   * 
   * @param constraints
   * @param travelTimes
   * @param localResults
   * @return
   * @throws ServiceException
   */
  public List<TimedPlaceBean> getLocalPaths(
      ConstraintsBean constraints,
      MinTravelTimeToStopsBean travelTimes, List<LocalSearchResult> localResults)
      throws ServiceException;
}
