package org.onebusaway.webapp.gwt.oba_application.search;

import org.onebusaway.geospatial.model.CoordinateBounds;

public interface LocalSearchProvider {

  public void search(CoordinateBounds bounds, String query, String category,
      LocalSearchCallback callback);

}
