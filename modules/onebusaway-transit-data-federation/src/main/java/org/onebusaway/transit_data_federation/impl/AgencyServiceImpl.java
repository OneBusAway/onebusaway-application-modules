package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.ExtendedGtfsRelationalDao;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AgencyServiceImpl implements AgencyService {

  @Autowired
  private ExtendedGtfsRelationalDao _dao;

  @Cacheable
  public Map<String, CoordinatePoint> getAgencyIdsAndCenterPoints() {
    
    Map<String, CoordinateBounds> agencyIdsAndBounds = _dao.getAgencyIdsAndBounds();
    
    Map<String,CoordinatePoint> agencyIdsAndCenterPoints = new HashMap<String, CoordinatePoint>();
    
    for( Map.Entry<String,CoordinateBounds> entry : agencyIdsAndBounds.entrySet() ) {
      CoordinateBounds bounds = entry.getValue();
      double lat = (bounds.getMinLat() + bounds.getMaxLat())/2;
      double lon = (bounds.getMinLon() + bounds.getMaxLon())/2;
      agencyIdsAndCenterPoints.put(entry.getKey(), new CoordinatePoint(lat,lon));
    }
    
    return agencyIdsAndCenterPoints;
  }

  @Cacheable
  public Map<String, CoordinateBounds> getAgencyIdsAndCoverageAreas() {
    return _dao.getAgencyIdsAndBounds();
  }
}
