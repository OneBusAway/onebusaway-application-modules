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

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

public abstract class VehicleState extends AtStopState {

  private final StopTimeInstance _sti;

  public VehicleState(StopTimeInstance sti, boolean arrival) {
    super(arrival ? sti.getArrivalTime() : sti.getDepartureTime(), sti.getStopTime().getStopTime().getStop());
    _sti = sti;
  }

  public StopTimeInstance getStopTimeInstance() {
    return _sti;
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj))
      return false;
    VehicleState vs = (VehicleState) obj;
    return _sti.equals(vs._sti);
  }

  @Override
  public int hashCode() {
    return super.hashCode() + _sti.hashCode();
  }
}