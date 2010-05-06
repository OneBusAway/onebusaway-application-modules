package org.onebusaway.oba.web.standard.client.search;

import org.onebusaway.oba.web.common.client.model.LocationBounds;

public interface LocalSearchProvider {

  public void search(LocationBounds bounds, String query, String category,
      LocalSearchCallback callback);

}
