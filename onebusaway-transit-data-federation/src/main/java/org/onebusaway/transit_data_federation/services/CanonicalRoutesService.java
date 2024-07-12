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
package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.AgencyServiceInterval;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteGroupingBean;


/**
 * Service interface for canonical (ideal) route representations.
 */
public interface CanonicalRoutesService {

  public static final String CANONICAL_TYPE = "canonical";
  public static final String HEURISTIC_TYPE = "heuristic";
  public static final String DIRECTION_TYPE = "direction";
  ListBean<RouteGroupingBean> getCanonicalOrMergedRoute(AgencyServiceInterval serviceInterval, AgencyAndId routeId);
}
