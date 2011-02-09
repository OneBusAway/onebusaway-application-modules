package org.onebusaway.presentation.services;

import org.onebusaway.presentation.model.DefaultSearchLocation;

public interface DefaultSearchLocationService {

  public DefaultSearchLocation getDefaultSearchLocationForCurrentUser();

  public void setDefaultLocationForCurrentUser(String locationName, double lat,
      double lon);

  public void clearDefaultLocationForCurrentUser();
}
