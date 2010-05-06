package org.onebusaway.tripplanner.model;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.gtdf.model.Trip;

public class BlockTransferState extends TripState {

  private final Trip _nextTrip;
  
  private final Point _location;

  public BlockTransferState(long currentTime,
      Trip nextTrip, Point location) {
    super(currentTime);
    _nextTrip = nextTrip;
    _location = location;
  }

  public Trip getNextTrip() {
    return _nextTrip;
  }

  @Override
  public Point getLocation() {
    return _location;
  }

  @Override
  public String toString() {
    return "blockTransfer(ts=" + getCurrentTimeString() + " trip="
        + _nextTrip.getId() + " route=" + _nextTrip.getRoute().getShortName()
        + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj))
      return false;
    BlockTransferState bs = (BlockTransferState) obj;
    return _nextTrip.equals(bs._nextTrip);
  }

  @Override
  public int hashCode() {
    return super.hashCode() + _nextTrip.hashCode();
  }

}
