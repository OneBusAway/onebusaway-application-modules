package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.container.cache.CacheableArgument;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data_federation.services.beans.GeospatialBeanService;
import org.onebusaway.transit_data_federation.services.beans.NearbyStopsBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class NearbyStopsBeanServiceImpl implements NearbyStopsBeanService {

  private GeospatialBeanService _geospatialBeanService;

  @Autowired
  public void setGeospatialBeanService(
      GeospatialBeanService geospatialBeanService) {
    _geospatialBeanService = geospatialBeanService;
  }

  @Cacheable
  public List<AgencyAndId> getNearbyStops(
      @CacheableArgument(keyProperty = "id") StopBean stopBean, double radius) {

    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(
        stopBean.getLat(), stopBean.getLon(), radius);
    List<AgencyAndId> ids = _geospatialBeanService.getStopsByBounds(bounds);

    List<AgencyAndId> excludingSource = new ArrayList<AgencyAndId>();

    for (AgencyAndId id : ids) {
      if (!ApplicationBeanLibrary.getId(id).equals(stopBean.getId()))
        excludingSource.add(id);
    }
    return excludingSource;
  }
}
