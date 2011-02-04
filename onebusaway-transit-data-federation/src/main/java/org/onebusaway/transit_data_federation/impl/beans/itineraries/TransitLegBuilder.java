package org.onebusaway.transit_data_federation.impl.beans.itineraries;

import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

class TransitLegBuilder {

  private BlockInstance blockInstance = null;

  private BlockTripEntry blockTrip = null;

  private long scheduledDepartureTime;
  private long scheduledArrivalTime;

  private long predictedDepartureTime;
  private long predictedArrivalTime;

  private ArrivalAndDepartureInstance fromStop;
  private ArrivalAndDepartureInstance toStop;

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

  public long getScheduledDepartureTime() {
    return scheduledDepartureTime;
  }

  public void setScheduledDepartureTime(long scheduledDepartureTime) {
    this.scheduledDepartureTime = scheduledDepartureTime;
  }

  public long getScheduledArrivalTime() {
    return scheduledArrivalTime;
  }

  public void setScheduledArrivalTime(long scheduledArrivalTime) {
    this.scheduledArrivalTime = scheduledArrivalTime;
  }

  public long getPredictedDepartureTime() {
    return predictedDepartureTime;
  }

  public void setPredictedDepartureTime(long predictedDepartureTime) {
    this.predictedDepartureTime = predictedDepartureTime;
  }

  public long getPredictedArrivalTime() {
    return predictedArrivalTime;
  }

  public void setPredictedArrivalTime(long predictedArrivalTime) {
    this.predictedArrivalTime = predictedArrivalTime;
  }

  public ArrivalAndDepartureInstance getFromStop() {
    return fromStop;
  }

  public void setFromStop(ArrivalAndDepartureInstance fromStop) {
    this.fromStop = fromStop;
  }

  public ArrivalAndDepartureInstance getToStop() {
    return toStop;
  }

  public void setToStop(ArrivalAndDepartureInstance toStop) {
    this.toStop = toStop;
  }

  public long getBestDepartureTime() {
    if (predictedDepartureTime != 0)
      return predictedDepartureTime;
    return scheduledDepartureTime;
  }

  public long getBestArrivalTime() {
    if (predictedArrivalTime != 0)
      return predictedArrivalTime;
    return scheduledArrivalTime;
  }
}