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
package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data_federation.services.beans.GeospatialBeanService;
import org.onebusaway.transit_data_federation.services.beans.NearbyStopsBeanService;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class NearbyStopsBeanServiceImpl implements NearbyStopsBeanService {

  private GeospatialBeanService _geospatialBeanService;
  
  private Cache _nearbyStopsCache;

  @Autowired
  public void setGeospatialBeanService(
      GeospatialBeanService geospatialBeanService) {
    _geospatialBeanService = geospatialBeanService;
  }
  
  public void setNearbyStopsCache(Cache nearbyStopsCache){
    _nearbyStopsCache = nearbyStopsCache;
  }

  public List<AgencyAndId> getNearbyStops(StopBean stopBean, double radius) {

    if (_nearbyStopsCache == null)
      return getNearbyStopsUncached(stopBean, radius);

    String key = getCacheKey(stopBean, radius);
    Element element = _nearbyStopsCache.get(key);

    if (element == null) {

      List<AgencyAndId> values = getNearbyStopsUncached(stopBean,
          radius);

      element = new Element(key, values);
      _nearbyStopsCache.put(element);
    }

    return (List<AgencyAndId>) element.getValue();
  }
  
  public List<AgencyAndId> getNearbyStopsUncached(StopBean stopBean, double radius) {
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
  
  private String getCacheKey(StopBean stopBean, double radius) {
     return stopBean.getId();
  }
  
}
