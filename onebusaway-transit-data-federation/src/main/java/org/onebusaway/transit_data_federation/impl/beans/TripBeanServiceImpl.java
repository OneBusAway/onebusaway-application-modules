/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TripBeanServiceImpl implements TripBeanService {

  private TransitGraphDao _graph;

  private NarrativeService _narrativeService;

  private RouteBeanService _routeBeanService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao graph) {
    _graph = graph;
  }

  @Autowired
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  @Autowired
  public void setRouteBeanService(RouteBeanService routeBeanService) {
    _routeBeanService = routeBeanService;
  }

  @Cacheable
  public TripBean getTripForId(AgencyAndId tripId) {

    TripEntry tripEntry = _graph.getTripEntryForId(tripId);

    if (tripEntry == null)
      return null;

    AgencyAndId routeId = tripEntry.getRouteCollection().getId();
    RouteBean routeBean = _routeBeanService.getRouteForId(routeId);

    TripNarrative tripNarrative = _narrativeService.getTripForId(tripId);

    TripBean tripBean = new TripBean();

    tripBean.setId(ApplicationBeanLibrary.getId(tripId));

    tripBean.setTripShortName(tripNarrative.getTripShortName());
    tripBean.setTripHeadsign(tripNarrative.getTripHeadsign());
    tripBean.setRoute(routeBean);
    tripBean.setRouteShortName(tripNarrative.getRouteShortName());
    tripBean.setServiceId(ApplicationBeanLibrary.getId(tripEntry.getServiceId().getId()));

    AgencyAndId shapeId = tripEntry.getShapeId();
    if (shapeId != null && shapeId.hasValues())
      tripBean.setShapeId(ApplicationBeanLibrary.getId(shapeId));

    tripBean.setDirectionId(tripEntry.getDirectionId());
    tripBean.setTotalTripDistance(tripEntry.getTotalTripDistance());
    
    BlockEntry block = tripEntry.getBlock();
    tripBean.setBlockId(ApplicationBeanLibrary.getId(block.getId()));
    
    return tripBean;
  }
}
