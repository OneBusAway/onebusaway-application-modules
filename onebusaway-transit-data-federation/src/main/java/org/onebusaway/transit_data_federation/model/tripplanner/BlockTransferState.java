package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.transit_data_federation.services.tripplanner.BlockTripEntry;

public class BlockTransferState extends TripState {

  private final BlockTripEntry _prevTrip;

  private final BlockTripEntry _nextTrip;

  private long _serviceDate;

  public BlockTransferState(long currentTime, BlockTripEntry prevTrip, BlockTripEntry nextTrip, long serviceDate) {
    super(currentTime);
    _prevTrip = prevTrip;
    _nextTrip = nextTrip;
    _serviceDate = serviceDate;
  }

  public BlockTripEntry getPrevTrip() {
    return _prevTrip;
  }

  public BlockTripEntry getNextTrip() {
    return _nextTrip;
  }

  public long getServiceDate() {
    return _serviceDate;
  }

  @Override
  public String toString() {
    return "blockTransfer(ts=" + getCurrentTimeString() + " trip=" + _nextTrip + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj))
      return false;
    BlockTransferState bs = (BlockTransferState) obj;
    return _prevTrip.equals(bs._prevTrip) && _nextTrip.equals(bs._nextTrip) && _serviceDate == bs._serviceDate;
  }

  @Override
  public int hashCode() {
    return super.hashCode() + _prevTrip.hashCode() + _nextTrip.hashCode() + new Long(_serviceDate).hashCode();
  }

}
