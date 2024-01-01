/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopDirectionSwap;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.StopSwapService;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of wrong way concurrencies.
 */
@Component
public class StopSwapServiceImpl implements StopSwapService {

  private static Logger _log = LoggerFactory.getLogger(StopSwapServiceImpl.class);
  @Autowired
  private FederatedTransitDataBundle _bundle;

  @Autowired
  public TransitDataService _transitDataService;

  @Autowired
  public TransitGraphDao _dao;
  public void setTransitDataService(TransitDataService tds) {
    _transitDataService = tds;
  }

  private Map<StopDirectionSwapKey, StopDirectionSwap> _cache;
  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.TRANSIT_GRAPH)
  public void setup() throws IOException, ClassNotFoundException {
    File path = _bundle.getStopSwapPath();
    if (path.exists()) {
      _log.info("loading Stop Swap / Wrong Way Concurrencies at {}", path);
      try {
        _cache = ObjectSerializationLibrary.readObject(path);
      } catch (Throwable t) {
        // this is optional, don't let it fail the load
        _cache = new HashMap<>();
      }
    } else {
      // this index is optional, do not fail if not found
      _cache = new HashMap<>();
    }

  }

  @Override
  public StopDirectionSwap findStopDirectionSwap(AgencyAndId routeId, String directionId, AgencyAndId stopId) {
    return _cache.get(new StopDirectionSwapKey(routeId, directionId, stopId));
  }

}
