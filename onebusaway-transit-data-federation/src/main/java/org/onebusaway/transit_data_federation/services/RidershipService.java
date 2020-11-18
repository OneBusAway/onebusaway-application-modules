/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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
import org.onebusaway.transit_data.OccupancyStatusBean;
import org.onebusaway.transit_data_federation.model.bundle.HistoricalRidership;

import java.util.List;

public interface RidershipService {
    public List<HistoricalRidership> getAllHistoricalRiderships(long serviceDate);
    public List<HistoricalRidership> getHistoricalRidershipsForTrip(AgencyAndId tripId, long serviceDate);
    public List<HistoricalRidership> getHistoricalRidershipsForStop(AgencyAndId stopId, long serviceDate);
    public List<HistoricalRidership> getHistoricalRidershipsForRoute(AgencyAndId routeId, long serviceDate);
    public List<HistoricalRidership> getHistoricalRiderships(AgencyAndId routeId, AgencyAndId tripId, AgencyAndId stopId, long serviceDate);

    public List<OccupancyStatusBean> convertToOccupancyStatusBeans(List<HistoricalRidership> hrs);

}
