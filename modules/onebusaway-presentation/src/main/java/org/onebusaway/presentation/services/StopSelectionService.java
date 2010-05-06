package org.onebusaway.presentation.services;

import org.onebusaway.exceptions.InvalidSelectionServiceException;
import org.onebusaway.presentation.model.StopSelectionBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;

import java.util.List;

public interface StopSelectionService {

  public StopSelectionBean getSelectedStops(StopsForRouteBean stopsForRoute,
      List<Integer> selectionIndices) throws InvalidSelectionServiceException;
}
