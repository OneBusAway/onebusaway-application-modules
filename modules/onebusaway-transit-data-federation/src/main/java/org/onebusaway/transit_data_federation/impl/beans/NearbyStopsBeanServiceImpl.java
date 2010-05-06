package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data_federation.services.beans.GeospatialBeanService;
import org.onebusaway.transit_data_federation.services.beans.NearbyStopsBeanService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
class NearbyStopsBeanServiceImpl implements NearbyStopsBeanService {

  @Autowired
  private GeospatialBeanService _geospatialBeanService;

  @Cacheable
  public List<AgencyAndId> getNearbyStops(StopBean stopBean, double radius) {
    List<AgencyAndId> ids = _geospatialBeanService.getStopsByLocation(stopBean.getLat(), stopBean.getLon(), radius);
    List<AgencyAndId> excludingSource = new ArrayList<AgencyAndId>();
    for (AgencyAndId id : ids) {
      if (!ApplicationBeanLibrary.getId(id).equals(stopBean.getId()))
        excludingSource.add(id);
    }
    return excludingSource;
  }
}
