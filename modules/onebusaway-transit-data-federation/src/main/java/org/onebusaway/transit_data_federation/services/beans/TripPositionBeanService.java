package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.TripStatusBean;

public interface TripPositionBeanService {
  public ListBean<TripStatusBean> getActiveTripForBounds(CoordinateBounds bounds,
      long time);
}
