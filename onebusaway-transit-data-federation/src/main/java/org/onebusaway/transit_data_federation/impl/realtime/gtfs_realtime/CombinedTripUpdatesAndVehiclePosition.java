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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

class CombinedTripUpdatesAndVehiclePosition implements
    Comparable<CombinedTripUpdatesAndVehiclePosition> {

  public BlockDescriptor block;
  // finally! support for multiple tripUpdates per block
  private TreeSet<TripUpdate> tripUpdates = new TreeSet<>(new TripComparator());
  public void setTripUpdates(List<TripUpdate> updates) {
    tripUpdates.addAll(updates);
  }
  public List<TripUpdate> getTripUpdates() {
    return new ArrayList<>(tripUpdates);
  }
  public int getTripUpdatesSize() {
    return tripUpdates.size();
  }
  public VehiclePosition vehiclePosition;

  @Override
  public int compareTo(CombinedTripUpdatesAndVehiclePosition o) {
    return block.getBlockInstance().getBlock().getBlock().getId().compareTo(
        o.block.getBlockInstance().getBlock().getBlock().getId());
  }

  @Override
  public String toString() {
    return "block=" + block.toString() + " tripUpdates=" + tripUpdates.toString() + ((vehiclePosition != null) ? " vehiclePosition=" + vehiclePosition.toString() : "");
  }

  /*
   * We want the tripUpdates in temporal order for convenient usage inside
   * the TDS, but we don't want to take the expense of looking up schedule values.
   * We hence take the shortcut of comparing tripIds and assuming they increment
   * for later trips.  This is a big assumption but generally proves to be true.
   */
  private class TripComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
      if (o1 == o2)
        return 0;
      TripUpdate t1 = (TripUpdate)o1;
      TripUpdate t2 = (TripUpdate)o2;
      // we trivially compare trip Ids hoping they are sequential/increasing in a block
      return t1.getTrip().getTripId().compareTo(t2.getTrip().getTripId());
      }
  }
}