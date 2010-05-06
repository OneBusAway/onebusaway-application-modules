package org.onebusaway.webapp.gwt.oba_application.control;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.oba_application.model.TimedLocalSearchResult;

import com.google.gwt.maps.client.geom.LatLng;

import java.util.List;

public interface CommonControl {

  public void handleContext(Context context);

  public void setQueryLocation(LatLng point);

  public void search(String resultId, List<CoordinateBounds> searchBounds);

  /*****************************************************************************
   * 
   ****************************************************************************/

  public void filterResults(Filter<TimedLocalSearchResult> filter);

  public void clearActiveSearchResult();

  public void setActiveSearchResult(TimedLocalSearchResult result);

  public void getDirectionsToPlace(LocalSearchResult place);

}
