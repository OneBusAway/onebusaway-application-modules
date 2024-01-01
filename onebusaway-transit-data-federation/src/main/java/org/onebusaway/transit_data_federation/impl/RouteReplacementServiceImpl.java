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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.RouteReplacementService;

import java.util.HashMap;
import java.util.Map;

/**
 * Translate internal routeId into public GTFS routeId so GTFS-RT can match GTFS.
 */
public class RouteReplacementServiceImpl implements RouteReplacementService {

  private Map<String, String> remaps = new HashMap<>();
  @Override
  public boolean containsRoute(AgencyAndId routeId) {
    return remaps.containsKey(routeId.getId());
  }

  @Override
  public AgencyAndId replace(AgencyAndId routeId) {
    String remapId = remaps.get(routeId.getId());
    return new AgencyAndId(routeId.getAgencyId(), remapId);
  }

  @Override
  public void putAll(Map<String, String> remaps) {
    this.remaps.putAll(remaps);
  }
}
