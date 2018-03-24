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
package org.onebusaway.transit_data_federation.model;

import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

/**
 * A trip instance is the combination of a {@link TripEntry} and a service date
 * for which that trip is active. The "service date" is the "midnight time" from
 * which the {@link StopTimeEntry} entries are relative.
 * 
 * @author bdferris
 * @see TripEntry
 */
public class TripInstance {

  private final TripEntry _trip;

  private final long _serviceDate;

  public TripInstance(TripEntry trip, long serviceDate) {
    if (trip == null)
      throw new IllegalArgumentException();
    _trip = trip;
    _serviceDate = serviceDate;
  }

  public TripEntry getTrip() {
    return _trip;
  }

  /**
   * The service date that the trip instance is operating. This is the
   * "midnight" time relative to the stop times for the trip.
   * 
   * @return the service date on which the trip is operating (Unix-time)
   */
  public long getServiceDate() {
    return _serviceDate;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (_serviceDate ^ (_serviceDate >>> 32));
    result = prime * result + ((_trip == null) ? 0 : _trip.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof TripInstance))
      return false;
    TripInstance other = (TripInstance) obj;
    if (_serviceDate != other._serviceDate)
      return false;
    if (!_trip.equals(other._trip))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return _trip.toString() + " " + _serviceDate;
  }

}
