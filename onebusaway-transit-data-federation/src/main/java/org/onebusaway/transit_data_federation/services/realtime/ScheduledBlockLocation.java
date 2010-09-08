package org.onebusaway.transit_data_federation.services.realtime;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;

public class ScheduledBlockLocation {

  private TripEntry activeTrip;

  private CoordinatePoint position;

  private double distanceAlongBlock;

  private StopTimeEntry closestStop;

  private int closestStopTimeOffset;

  public TripEntry getActiveTrip() {
    return activeTrip;
  }

  public void setActiveTrip(TripEntry activeTrip) {
    this.activeTrip = activeTrip;
  }

  public CoordinatePoint getPosition() {
    return position;
  }

  public void setPosition(CoordinatePoint position) {
    this.position = position;
  }

  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public void setDistanceAlongBlock(double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public StopTimeEntry getClosestStop() {
    return closestStop;
  }

  public void setClosestStop(StopTimeEntry closestStop) {
    this.closestStop = closestStop;
  }

  public int getClosestStopTimeOffset() {
    return closestStopTimeOffset;
  }

  public void setClosestStopTimeOffset(int closestStopTimeOffset) {
    this.closestStopTimeOffset = closestStopTimeOffset;
  }
}