package org.onebusaway.presentation.impl;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.presentation.services.ServiceAreaService;
import org.onebusaway.users.client.model.UserBean;

public class DefaultServiceAreaServiceImpl implements ServiceAreaService {

  private static final double DEFAULT_SEARCH_RADIUS = 10000;

  private double _searchRadius = DEFAULT_SEARCH_RADIUS;

  private CoordinateBounds _defaultBounds;

  public double getSearchRadius() {
    return _searchRadius;
  }

  public void setSearchRadius(double searchRadius) {
    _searchRadius = searchRadius;
  }

  public CoordinateBounds getDefaultBounds() {
    return _defaultBounds;
  }

  public void setDefaultBounds(CoordinateBounds bounds) {
    _defaultBounds = bounds;
  }

  public CoordinateBounds getServiceArea(UserBean currentUser) {

    if (currentUser != null && currentUser.hasDefaultLocation() ) {

      double lat = currentUser.getDefautLocationLat();
      double lon = currentUser.getDefaultLocationLon();

      return SphericalGeometryLibrary.bounds(lat, lon, _searchRadius);
    }

    return _defaultBounds;
  }
}
