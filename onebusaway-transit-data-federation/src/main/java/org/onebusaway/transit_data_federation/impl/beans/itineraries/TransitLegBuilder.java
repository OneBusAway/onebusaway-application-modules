package org.onebusaway.transit_data_federation.impl.beans.itineraries;

import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

class TransitLegBuilder {

  private BlockInstance blockInstance = null;

  private BlockTripEntry blockTrip = null;

  private long startTime;
  private long endTime;

  private StopTimeInstance fromStop;
  private StopTimeInstance toStop;

  public BlockInstance getBlockInstance() {
    return blockInstance;
  }

  public void setBlockInstance(BlockInstance blockInstance) {
    this.blockInstance = blockInstance;
  }

  public BlockTripEntry getBlockTrip() {
    return blockTrip;
  }

  public void setBlockTrip(BlockTripEntry blockTrip) {
    this.blockTrip = blockTrip;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public StopTimeInstance getFromStop() {
    return fromStop;
  }

  public void setFromStop(StopTimeInstance fromStop) {
    this.fromStop = fromStop;
  }

  public StopTimeInstance getToStop() {
    return toStop;
  }

  public void setToStop(StopTimeInstance toStop) {
    this.toStop = toStop;
  }
}