package org.onebusaway.webapp.services.oba;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTransitTimeResult;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;

import java.util.List;

public interface OneBusAwayService {

  public MinTransitTimeResult getMinTravelTimeToStopsFrom(double lat,
      double lon, OneBusAwayConstraintsBean constraints, int timeSegmentSize)
      throws ServiceException;

  public List<TimedPlaceBean> getLocalPaths(String resultId,
      List<LocalSearchResult> localResults) throws ServiceException;

  public void clearCurrentResult();
}
