/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceAreaServiceImpl implements ServiceAreaService {

  private static Logger _log = LoggerFactory.getLogger(ServiceAreaServiceImpl.class);
  
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
      // the TDS may not be initialized at this point, perform work on thread
      BackgroundThread bt = new BackgroundThread(_transitDataService, this);
      new Thread(bt).start();
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
  
  private static class BackgroundThread implements Runnable {

    private TransitDataService _tds;
    private ServiceAreaServiceImpl _sas;
    public BackgroundThread(TransitDataService tds, ServiceAreaServiceImpl sas) {
      _tds = tds;
      _sas = sas;
    }
    @Override
    public void run() {
      
      List<AgencyWithCoverageBean> agenciesWithCoverage = _tds.getAgenciesWithCoverage();
      CoordinateBounds bounds = new CoordinateBounds();

      for (AgencyWithCoverageBean bean : agenciesWithCoverage) {
        double lat = bean.getLat();
        double lon = bean.getLon();
        double latSpan = bean.getLatSpan() / 2;
        double lonSpan = bean.getLonSpan() / 2;
        bounds.addPoint(lat - latSpan, lon - lonSpan);
        bounds.addPoint(lat + latSpan, lon + lonSpan);
      }

      if (!bounds.isEmpty()) {
        _log.info("setting default agency bounds to " + bounds);
        _sas.setDefaultBounds(bounds);
      }

    }
    
  }
}
