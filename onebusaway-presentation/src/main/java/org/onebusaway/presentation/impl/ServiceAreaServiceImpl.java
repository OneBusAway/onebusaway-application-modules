package org.onebusaway.presentation.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.presentation.model.DefaultSearchLocation;
import org.onebusaway.presentation.services.DefaultSearchLocationService;
import org.onebusaway.presentation.services.ServiceAreaService;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceAreaServiceImpl implements ServiceAreaService {

  private static final double DEFAULT_SEARCH_RADIUS = 20000;

  private TransitDataService _transitDataService;

  private DefaultSearchLocationService _defaultSearchLocationService;

  private double _searchRadius = DEFAULT_SEARCH_RADIUS;

  private CoordinateBounds _defaultBounds;

  private boolean _calculateDefaultBoundsFromAgencyCoverage = true;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setDefaultSearchLocationService(
      DefaultSearchLocationService defaultSearchLocationService) {
    _defaultSearchLocationService = defaultSearchLocationService;
  }

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

  public void setCalculateDefaultBoundsFromAgencyCoverage(
      boolean calculateDefaultBoundsFromAgencyCoverage) {
    _calculateDefaultBoundsFromAgencyCoverage = calculateDefaultBoundsFromAgencyCoverage;
  }

  @PostConstruct
  public void setup() {

    if (_calculateDefaultBoundsFromAgencyCoverage) {
      List<AgencyWithCoverageBean> agenciesWithCoverage = _transitDataService.getAgenciesWithCoverage();
      CoordinateBounds bounds = new CoordinateBounds();

      for (AgencyWithCoverageBean bean : agenciesWithCoverage) {
        double lat = bean.getLat();
        double lon = bean.getLon();
        double latSpan = bean.getLatSpan() / 2;
        double lonSpan = bean.getLonSpan() / 2;
        bounds.addPoint(lat - latSpan, lon - lonSpan);
        bounds.addPoint(lat + latSpan, lon + lonSpan);
      }

      if (!bounds.isEmpty())
        _defaultBounds = bounds;
    }
  }

  /****
   * {@link ServiceAreaService}
   ****/
  
  @Override
  public boolean hasDefaultServiceArea() {
    return _defaultBounds != null;
  }

  @Override
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
