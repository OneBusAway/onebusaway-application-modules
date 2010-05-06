package org.onebusaway.oba.web.standard.client.control;

import org.onebusaway.common.web.common.client.context.Context;
import org.onebusaway.oba.web.common.client.model.LocalSearchResult;
import org.onebusaway.oba.web.common.client.model.LocationBounds;
import org.onebusaway.oba.web.common.client.model.OneBusAwayConstraintsBean;
import org.onebusaway.oba.web.standard.client.model.TimedLocalSearchResult;

import com.google.gwt.maps.client.geom.LatLng;

import java.util.List;

public interface OneBusAwayStandardPresenter {

  public void handleContext(Context context);

  public void query(String query, String locationQuery, LatLng location, OneBusAwayConstraintsBean constraints);

  public void setQueryLocation(LatLng point);

  public void search(String resultId, List<LocationBounds> searchBounds);

  /*****************************************************************************
   * 
   ****************************************************************************/

  public void filterResults(Filter<TimedLocalSearchResult> filter);

  public void clearActiveSearchResult();

  public void setActiveSearchResult(TimedLocalSearchResult result);

  public void getDirectionsToPlace(LocalSearchResult place);

}
