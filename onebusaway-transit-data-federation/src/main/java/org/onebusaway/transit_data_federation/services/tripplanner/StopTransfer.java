package org.onebusaway.transit_data_federation.services.tripplanner;

public final class StopTransfer {

  private final StopEntry stop;

  private final int minTransferTime;

  private final double distance;

  public StopTransfer(StopEntry stop, int minTransferTime, double distance) {
    this.stop = stop;
    this.minTransferTime = minTransferTime;
    this.distance = distance;
  }

  public StopEntry getStop() {
    return stop;
  }

  public int getMinTransferTime() {
    return minTransferTime;
  }

  public double getDistance() {
    return distance;
  }
}
