package org.onebusaway.transit_data_federation.impl.transit_graph;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

public class BlockTripEntryImpl implements BlockTripEntry, Serializable {

  private static final long serialVersionUID = 1L;

  private BlockConfigurationEntry blockConfiguration;

  private TripEntry trip;

  private int accumulatedStopTimeIndex;

  private int accumulatedSlackTime;

  private double distanceAlongBlock;

  private BlockTripEntry previousTrip;

  private BlockTripEntry nextTrip;

  public void setTrip(TripEntry trip) {
    this.trip = trip;
  }

  public void setBlockConfiguration(BlockConfigurationEntry blockConfiguration) {
    this.blockConfiguration = blockConfiguration;
  }

  public void setAccumulatedStopTimeIndex(int accumulatedStopTimeIndex) {
    this.accumulatedStopTimeIndex = accumulatedStopTimeIndex;
  }

  public void setAccumulatedSlackTime(int accumulatedSlackTime) {
    this.accumulatedSlackTime = accumulatedSlackTime;
  }

  public void setDistanceAlongBlock(double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public void setPreviousTrip(BlockTripEntry previousTrip) {
    this.previousTrip = previousTrip;
  }

  public void setNextTrip(BlockTripEntry nextTrip) {
    this.nextTrip = nextTrip;
  }

  /****
   * {@link BlockTripEntry} Interface
   ****/

  @Override
  public BlockConfigurationEntry getBlockConfiguration() {
    return blockConfiguration;
  }

  @Override
  public TripEntry getTrip() {
    return trip;
  }

  @Override
  public List<BlockStopTimeEntry> getStopTimes() {
    List<BlockStopTimeEntry> stopTimes = blockConfiguration.getStopTimes();
    int toIndex = stopTimes.size();
    if (nextTrip != null)
      toIndex = nextTrip.getAccumulatedStopTimeIndex();
    return stopTimes.subList(accumulatedStopTimeIndex, toIndex);
  }

  @Override
  public int getAccumulatedStopTimeIndex() {
    return accumulatedStopTimeIndex;
  }

  @Override
  public int getAccumulatedSlackTime() {
    return accumulatedSlackTime;
  }

  @Override
  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  @Override
  public BlockTripEntry getPreviousTrip() {
    return previousTrip;
  }

  @Override
  public BlockTripEntry getNextTrip() {
    return nextTrip;
  }

  @Override
  public int getArrivalTimeForIndex(int stopIndex) {
    return trip.getStopTimes().get(stopIndex).getArrivalTime();
  }

  @Override
  public int getDepartureTimeForIndex(int stopIndex) {
    return trip.getStopTimes().get(stopIndex).getDepartureTime();
  }

  @Override
  public double getDistanceAlongBlockForIndex(int stopIndex) {
    return distanceAlongBlock
        + trip.getStopTimes().get(stopIndex).getShapeDistTraveled();
  }

  @Override
  public String toString() {
    return trip.getId().toString();
  }
}
