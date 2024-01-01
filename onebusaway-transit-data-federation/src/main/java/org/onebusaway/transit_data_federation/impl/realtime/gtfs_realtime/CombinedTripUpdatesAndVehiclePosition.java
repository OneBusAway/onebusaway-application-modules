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

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    return "block=" + (block!=null?block.toString():"empty") + " tripUpdates=" + tripUpdates.toString() + ((vehiclePosition != null) ? " vehiclePosition=" + vehiclePosition.toString() : "");
  }

  /*
   * We want the tripUpdates in temporal order for convenient usage inside
   * the TDS, but we need to support empty trip updates.
   * So now we enforce start_time property on storage and sort by that!
   */
  private class TripComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
      if (o1 == o2)
        return 0;
      TripUpdate t1 = (TripUpdate)o1;
      TripUpdate t2 = (TripUpdate)o2;
      if (verifyStartTime(t1) && verifyStartTime(t2)) {
        return Math.toIntExact(parseDate(t1.getTrip().getStartDate(), t1.getTrip().getStartTime()) -
                parseDate(t2.getTrip().getStartDate(), t2.getTrip().getStartTime()));
      } else {
        // try and do something reasonable without a start date
        return t1.getTrip().getTripId().compareTo(t2.getTrip().getTripId());
      }
    }

    private long parseDate(String startDate, String startTime) throws IllegalStateException {
      if (startDate == null || startDate.length() == 0)
        startDate = "00000000";
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
      try {
        return sdf.parse(startDate + " " + startTime).getTime();
      } catch (ParseException e) {
        throw new IllegalStateException("invalid date format: " + startDate + " " + startTime);
      }
    }

    private boolean verifyStartTime(TripUpdate t) {
      if (!t.hasTrip() || !t.getTrip().hasStartTime())
        return false;
      return true;
    }

  }
}