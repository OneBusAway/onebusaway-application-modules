/**
 * Copyright (C) 2024 Cambridge Systematics Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime;

import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.*;

import java.util.Date;
import java.util.List;

/**
 * Methods supporting pruning of dynamic objects.
 */
public class DynamicCache {

  private long lastPruneTime = 0l;
  private long pruneIntervalMillis = 1 * 60 * 60 * 1000; // 1 hour

  private long windowInMillis = 90 * 60 * 1000; // 90 minutes

  public void setPruneIntervalMillis(long millis) {
    this.pruneIntervalMillis = millis;
  }

  public void setWindowInMillis(long millis) {
    this.windowInMillis = millis;
  }

  protected boolean needsPrune(long currentTime) {
    return currentTime > lastPruneTime + pruneIntervalMillis;
  }
  protected void resetStats(long currentTime) {
    lastPruneTime = currentTime;
  }

  public int getEffectiveTime(long currentTime) {
    return Math.toIntExact((currentTime - new ServiceDate(new Date(currentTime)).getAsDate().getTime())/1000);
  }
  protected boolean isExpired(ServiceInterval range, int effectiveTime) {
    int minBounds = Math.toIntExact(range.getMinArrival() - windowInMillis / 1000);
    int maxBounds = Math.toIntExact(range.getMaxDeparture() + windowInMillis / 1000);
    if (minBounds >= effectiveTime || maxBounds <= effectiveTime)
      return true;
    return false;
  }

  protected boolean isExpired(BlockTripIndex blockTripIndex, long currentTime, int effectiveTime) {
    if (isExpired(blockTripIndex.getServiceIntervalBlock().getRange(), effectiveTime)) {
      return true;
    }
    return false;
  }

  protected boolean isExpired(BlockInstance blockInstance, int effectiveTime) {
    return isExpired(blockInstance.getBlock(), effectiveTime);
  }

  protected boolean isExpired(BlockStopTimeIndex stopTimeIndex, long currentTime, int effectiveTime) {
    return isExpired(stopTimeIndex.getServiceInterval(), effectiveTime);
  }

  protected boolean isExpired(BlockEntry blockEntry, int effectiveTime) {
    return isExpired(blockEntry.getConfigurations().get(0), effectiveTime);
  }

  protected boolean isExpired(BlockConfigurationEntry entry, int effectiveTime) {
    List<BlockStopTimeEntry> stopTimes = entry.getBlock().getConfigurations().get(0).getStopTimes();
    int size = stopTimes.size();
    ServiceInterval serviceInterval = new ServiceInterval(getArrivalOrDeparture(stopTimes.get(0).getStopTime()),
            getDepartureOrArrival(stopTimes.get(size - 1).getStopTime()));
    return isExpired(serviceInterval, effectiveTime);
  }

  private int getDepartureOrArrival(StopTimeEntry stopTime) {
    if (stopTime.getDepartureTime() > 0)
      return stopTime.getDepartureTime();
    return stopTime.getArrivalTime();
  }

  private int getArrivalOrDeparture(StopTimeEntry stopTime) {
    if (stopTime.getArrivalTime() > 0)
      return stopTime.getArrivalTime();
    return stopTime.getDepartureTime();
  }

  protected boolean isExpired(TripEntry trip, int effectiveTime) {
    List<StopTimeEntry> stopTimes = trip.getStopTimes();
    int size = stopTimes.size();
    ServiceInterval serviceInterval = new ServiceInterval(getArrivalOrDeparture(stopTimes.get(0)),
            getDepartureOrArrival(stopTimes.get(size - 1)));
    return isExpired(serviceInterval, effectiveTime);
  }

}
