package org.onebusaway.tripplanner.model;

import com.vividsolutions.jts.geom.Point;

public class BlockTransferState extends TripState {

  private final String _prevTripId;

  private final String _nextTripId;

  private long _serviceDate;

  public BlockTransferState(long currentTime, Point location, String prevTripId, String nextTripId, long serviceDate) {
    super(currentTime, location);
    _prevTripId = prevTripId;
    _nextTripId = nextTripId;
    _serviceDate = serviceDate;
  }

  public String getPrevTripId() {
    return _prevTripId;
  }

  public String getNextTripId() {
    return _nextTripId;
  }

  public long getServiceDate() {
    return _serviceDate;
  }

  @Override
  public String toString() {
    return "blockTransfer(ts=" + getCurrentTimeString() + " trip=" + _nextTripId + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj))
      return false;
    BlockTransferState bs = (BlockTransferState) obj;
    return _prevTripId.equals(bs._prevTripId) && _nextTripId.equals(bs._nextTripId) && _serviceDate == bs._serviceDate;
  }

  @Override
  public int hashCode() {
    return super.hashCode() + _prevTripId.hashCode() + _nextTripId.hashCode() + new Long(_serviceDate).hashCode();
  }

}
