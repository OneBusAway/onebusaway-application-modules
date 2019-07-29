/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.impl.transit_graph;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

public class BlockConfigurationEntryImpl implements BlockConfigurationEntry,
    Serializable {

  private static final long serialVersionUID = 1L;

  private final BlockEntry block;

  private final ServiceIdActivation serviceIds;

  private final List<BlockTripEntry> trips;

  private final double totalBlockDistance;

  private final BlockStopTimeList blockStopTimes = new BlockStopTimeList();

  private final int[] tripIndices;

  private final int[] accumulatedStopTimeIndices;

  /**
   * We make this one field non-final because it makes it easier to add
   * Frequency information after the fact.
   */
  private List<FrequencyEntry> frequencies;

  private BlockConfigurationEntryImpl(Builder builder) {
    this.block = builder.block;
    this.serviceIds = builder.serviceIds;
    this.trips = builder.computeBlockTrips(this);
    this.frequencies = builder.frequencies;
    this.totalBlockDistance = builder.computeTotalBlockDistance();
    this.tripIndices = builder.computeTripIndices();
    this.accumulatedStopTimeIndices = builder.computeAccumulatedStopTimeIndices();
  }

  public static Builder builder() {
    return new Builder();
  }

  public void setFrequencies(List<FrequencyEntry> frequencies) {
    this.frequencies = frequencies;
  }

  /****
   * {@link BlockConfigurationEntry} Interface
   ****/

  @Override
  public BlockEntry getBlock() {
    return block;
  }

  @Override
  public ServiceIdActivation getServiceIds() {
    return serviceIds;
  }

  @Override
  public List<BlockTripEntry> getTrips() {
    return trips;
  }

  @Override
  public List<FrequencyEntry> getFrequencies() {
    return frequencies;
  }

  @Override
  public List<BlockStopTimeEntry> getStopTimes() {
    return blockStopTimes;
  }

  @Override
  public double getTotalBlockDistance() {
    return totalBlockDistance;
  }

  @Override
  public int getArrivalTimeForIndex(int index) {
    StopTimeEntry stopTime = getStopTimeForIndex(index);
    return stopTime.getArrivalTime();
  }

  @Override
  public int getDepartureTimeForIndex(int index) {
    StopTimeEntry stopTime = getStopTimeForIndex(index);
    return stopTime.getDepartureTime();
  }

  @Override
  public double getDistanceAlongBlockForIndex(int index) {

    int tripIndex = tripIndices[index];

    BlockTripEntry blockTrip = trips.get(tripIndex);
    TripEntry trip = blockTrip.getTrip();

    List<StopTimeEntry> stopTimes = trip.getStopTimes();
    int stopTimeIndex = index - accumulatedStopTimeIndices[tripIndex];
    StopTimeEntry stopTime = stopTimes.get(stopTimeIndex);

    return blockTrip.getDistanceAlongBlock() + stopTime.getShapeDistTraveled();
  }

  @Override
  public OccupancyStatus getOccupancyForIndex(int index) {
    StopTimeEntry stopTime = getStopTimeForIndex(index);
    return stopTime.getHistoricalOccupancy();
  }

  @Override
  public String toString() {
    return "BlockConfiguration [block=" + block.getId() + " serviceIds="
        + serviceIds + "]";
  }

  public static class Builder {

    private BlockEntry block;

    private ServiceIdActivation serviceIds;

    private List<TripEntry> trips;

    private List<FrequencyEntry> frequencies;

    private double[] tripGapDistances;

    private Builder() {

    }

    public void setBlock(BlockEntry block) {
      this.block = block;
    }

    public void setServiceIds(ServiceIdActivation serviceIds) {
      this.serviceIds = serviceIds;
    }

    public List<TripEntry> getTrips() {
      return trips;
    }

    public void setTrips(List<TripEntry> trips) {
      this.trips = trips;
    }

    public List<FrequencyEntry> getFrequencies() {
      return frequencies;
    }

    public void setFrequencies(List<FrequencyEntry> frequencies) {
      this.frequencies = frequencies;
    }

    public void setTripGapDistances(double[] tripGapDistances) {
      this.tripGapDistances = tripGapDistances;
    }

    public BlockConfigurationEntry create() {
      return new BlockConfigurationEntryImpl(this);
    }

    private double computeTotalBlockDistance() {
      double distance = 0;
      for (int i = 0; i < trips.size(); i++) {
        TripEntry trip = trips.get(i);
        distance += trip.getTotalTripDistance() + tripGapDistances[i];
      }
      return distance;
    }

    private int[] computeTripIndices() {
      int n = 0;
      for (TripEntry trip : trips)
        n += trip.getStopTimes().size();
      int[] tripIndices = new int[n];
      int index = 0;
      for (int tripIndex = 0; tripIndex < trips.size(); tripIndex++) {
        TripEntry trip = trips.get(tripIndex);
        for (int i = 0; i < trip.getStopTimes().size(); i++)
          tripIndices[index++] = tripIndex;
      }
      return tripIndices;
    }

    private int[] computeAccumulatedStopTimeIndices() {
      int[] accumulatedStopTimeIndices = new int[trips.size()];
      int n = 0;
      for (int i = 0; i < trips.size(); i++) {
        accumulatedStopTimeIndices[i] = n;
        n += trips.get(i).getStopTimes().size();
      }
      return accumulatedStopTimeIndices;
    }

    private List<BlockTripEntry> computeBlockTrips(
        BlockConfigurationEntryImpl blockConfiguration) {

      ArrayList<BlockTripEntry> blockTrips = new ArrayList<BlockTripEntry>();
      short accumulatedStopTimeIndex = 0;
      int accumulatedSlackTime = 0;
      double distanceAlongBlock = 0;

      BlockTripEntryImpl prevTrip = null;
      StopTimeEntry prevTripStopTime = null;
      double prevTripAvgVelocity = 0;

      for (short i = 0; i < trips.size(); i++) {

        TripEntry tripEntry = trips.get(i);
        List<StopTimeEntry> stopTimes = tripEntry.getStopTimes();

        /**
         * See if there is any slack time in the schedule in the transition
         * between the two trips. We take the distance between the last stop of
         * the previous trip and the first stop of the next trip, along with the
         * average travel velocity from the previous trip, and compute the
         * estimated travel time. Any time that's left over is slack.
         */
        if (prevTripStopTime != null) {
          StopTimeEntry nextStopTime = stopTimes.get(0);
          int slackTime = nextStopTime.getArrivalTime()
              - prevTripStopTime.getDepartureTime();
          double distance = (distanceAlongBlock - (prevTrip.getDistanceAlongBlock() + prevTripStopTime.getShapeDistTraveled()))
              + nextStopTime.getShapeDistTraveled();
          if (prevTripAvgVelocity > 0) {
            int timeToTravel = (int) (distance / prevTripAvgVelocity);
            slackTime -= Math.min(timeToTravel, slackTime);
          }

          accumulatedSlackTime += slackTime;
        }

        BlockTripEntryImpl blockTripEntry = new BlockTripEntryImpl();
        blockTripEntry.setTrip(tripEntry);
        blockTripEntry.setBlockConfiguration(blockConfiguration);
        blockTripEntry.setSequence(i);
        blockTripEntry.setAccumulatedStopTimeIndex(accumulatedStopTimeIndex);
        blockTripEntry.setAccumulatedSlackTime(accumulatedSlackTime);
        blockTripEntry.setDistanceAlongBlock(distanceAlongBlock);

        if (prevTrip != null) {
          prevTrip.setNextTrip(blockTripEntry);
          blockTripEntry.setPreviousTrip(prevTrip);
        }

        blockTrips.add(blockTripEntry);

        accumulatedStopTimeIndex += stopTimes.size();

        if (accumulatedSlackTime < 0)
          throw new IllegalStateException(
              "I didn't think this was possible, but the number of stop times in a particular block exceeded "
                  + Short.MAX_VALUE
                  + " causing a wrap-around in the accumulated stop time index: blockId="
                  + blockConfiguration.getBlock().getId());

        for (StopTimeEntry stopTime : stopTimes)
          accumulatedSlackTime += stopTime.getSlackTime();

        prevTripAvgVelocity = computeAverageTripTravelVelocity(stopTimes);

        distanceAlongBlock += tripEntry.getTotalTripDistance()
            + tripGapDistances[i];

        prevTrip = blockTripEntry;
        prevTripStopTime = stopTimes.get(stopTimes.size() - 1);

      }

      blockTrips.trimToSize();

      return blockTrips;
    }

    private double computeAverageTripTravelVelocity(
        List<StopTimeEntry> stopTimes) {

      int accumulatedTravelTime = 0;
      double accumulatedTravelDistance = 0;
      StopTimeEntry prevStopTime = null;

      for (StopTimeEntry stopTime : stopTimes) {
        if (prevStopTime != null) {
          accumulatedTravelTime += stopTime.getArrivalTime()
              - prevStopTime.getDepartureTime();
          accumulatedTravelDistance += stopTime.getShapeDistTraveled()
              - prevStopTime.getShapeDistTraveled();
        }
        prevStopTime = stopTime;
      }

      if (accumulatedTravelTime == 0)
        return 0;

      return accumulatedTravelDistance / accumulatedTravelTime;
    }
  }

  /*****
   * Private Methods
   ****/

  private StopTimeEntry getStopTimeForIndex(int index) {
    int tripIndex = tripIndices[index];

    BlockTripEntry blockTrip = trips.get(tripIndex);
    TripEntry trip = blockTrip.getTrip();

    List<StopTimeEntry> stopTimes = trip.getStopTimes();
    int stopTimeIndex = index - accumulatedStopTimeIndices[tripIndex];
    StopTimeEntry stopTime = stopTimes.get(stopTimeIndex);
    return stopTime;
  }

  private class BlockStopTimeList extends AbstractList<BlockStopTimeEntry>
      implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int size() {
      return tripIndices.length;
    }

    @Override
    public BlockStopTimeEntry get(int index) {

      int tripIndex = tripIndices[index];

      BlockTripEntry blockTrip = trips.get(tripIndex);
      TripEntry trip = blockTrip.getTrip();

      List<StopTimeEntry> stopTimes = trip.getStopTimes();
      int stopTimeIndex = index - accumulatedStopTimeIndices[tripIndex];
      StopTimeEntry stopTime = stopTimes.get(stopTimeIndex);

      boolean hasNextStop = index + 1 < tripIndices.length;

      return new BlockStopTimeEntryImpl(stopTime, index, blockTrip, hasNextStop);
    }
  }

}
