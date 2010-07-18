package org.onebusaway.presentation.impl;


import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.presentation.model.DefaultSearchLocation;
import org.onebusaway.presentation.services.DefaultSearchLocationService;
import org.onebusaway.presentation.services.ServiceAreaService;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultServiceAreaServiceImpl implements ServiceAreaService {

  private static final double DEFAULT_SEARCH_RADIUS = 20000;

  private double _searchRadius = DEFAULT_SEARCH_RADIUS;

  private CoordinateBounds _defaultBounds;

  private DefaultSearchLocationService _defaultSearchLocationService;

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

  @Autowired
  public void setDefaultSearchLocationService(
      DefaultSearchLocationService defaultSearchLocationService) {
    _defaultSearchLocationService = defaultSearchLocationService;
  }

  public CoordinateBounds getServiceArea() {

    DefaultSearchLocation location = _defaultSearchLocationService.getDefaultSearchLocationForCurrentUser();

    if (location != null) {

      double lat = location.getLat();
      double lon = location.getLon();

      return SphericalGeometryLibrary.bounds(lat, lon, _searchRadius);
    }

    return _defaultBounds;
  }
}
