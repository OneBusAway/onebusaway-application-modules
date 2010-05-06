package org.onebusaway.webapp.gwt.where_library.services;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.StopBean;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public interface StopsForRegionService {

  public void getStopsForRegion(CoordinateBounds bounds,
      AsyncCallback<List<StopBean>> callback);
}
