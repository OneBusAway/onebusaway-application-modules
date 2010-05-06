package org.onebusaway.transit_data_federation.services.oba;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;

import java.util.List;

public interface OneBusAwayService {
  public List<TimedPlaceBean> getLocalPaths(
      OneBusAwayConstraintsBean constraints,
      MinTravelTimeToStopsBean travelTimes, List<LocalSearchResult> localResults)
      throws ServiceException;
}
