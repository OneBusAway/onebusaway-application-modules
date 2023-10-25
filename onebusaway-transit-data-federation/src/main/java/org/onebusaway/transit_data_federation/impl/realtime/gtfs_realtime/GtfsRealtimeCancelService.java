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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;

import java.util.List;
import java.util.Set;

/**
 * Canceling service when GTFS-RT input is replacing the existing service.
 */
public interface GtfsRealtimeCancelService {

  Set<RouteEntry> findRoutesForIds(List<AgencyAndId> ids);
  List<TripDetailsBean> findActiveTripsForRoute(RouteEntry route, long timestamp);
  void cancel(List<TripDetailsBean> tripsToCancel);

  void cancelServiceForRoutes(List<AgencyAndId> routeIdsToCancel, long timestamp);
}
