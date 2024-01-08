/**
 * Copyright (C) 2024 Cambridge Systematics, Inc.
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

import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

/**
 * Utility methods for generating ServiceIntervals.
 */
public class ServiceIntervalHelper {

  public ServiceInterval getServiceIntervalForTrip(TripEntry trip) {
    int tripStartTime = trip.getStopTimes().get(0).getDepartureTime();
    int tripEndTime = -1;
    int position = trip.getStopTimes().size();
    // account for missing end times
    while (tripEndTime < 0) {
      position--;
      tripEndTime = trip.getStopTimes().get(position).getArrivalTime();
    }
    ServiceInterval tripInterval = new ServiceInterval(tripStartTime, tripEndTime);
    return tripInterval;
  }

  public ServiceInterval getServiceIntervalForTrip(BlockTripEntry trip) {
    return getServiceIntervalForTrip(trip.getTrip());
  }

}
