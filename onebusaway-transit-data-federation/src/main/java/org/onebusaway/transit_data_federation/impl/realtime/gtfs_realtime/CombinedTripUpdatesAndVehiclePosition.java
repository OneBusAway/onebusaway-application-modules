/**
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import java.util.List;

import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

class CombinedTripUpdatesAndVehiclePosition implements
    Comparable<CombinedTripUpdatesAndVehiclePosition> {

  public BlockDescriptor block;
  public List<TripUpdate> tripUpdates;
  public VehiclePosition vehiclePosition;
  public String bestTrip;

  @Override
  public int compareTo(CombinedTripUpdatesAndVehiclePosition o) {
    return block.getBlockInstance().getBlock().getBlock().getId().compareTo(
        o.block.getBlockInstance().getBlock().getBlock().getId());
  }

  @Override
  public String toString() {
    return "block=" + block.toString() + " tripUpdates=" + tripUpdates.toString() + ((vehiclePosition != null) ? " vehiclePosition=" + vehiclePosition.toString() : "");
  }
}