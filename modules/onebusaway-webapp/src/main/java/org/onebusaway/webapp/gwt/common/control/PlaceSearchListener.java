package org.onebusaway.webapp.gwt.common.control;

import java.util.List;

public interface PlaceSearchListener {
  
  public void handleSingleResult(Place place);

  public void handleNoResult();

  public void handleMultipleResults(List<Place> locations);

  public void handleError();
}
