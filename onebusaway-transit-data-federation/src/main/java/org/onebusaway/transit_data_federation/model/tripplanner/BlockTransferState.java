/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

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
