/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.services.realtime;

import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.InstanceState;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.model.StopTimeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Captures an arrival and departure time combination, as a concrete time, as
 * opposed to a relative time.
 * 
 * @author bdferris
 * 
 */
public class ArrivalAndDepartureTime {

  private static Logger _log = LoggerFactory.getLogger(ArrivalAndDepartureTime.class);
  
  private long arrivalTime;

  private long departureTime;

  public ArrivalAndDepartureTime(long arrivalTime, long departureTime) {
    this.arrivalTime = arrivalTime;
    this.departureTime = departureTime;
  }

  public long getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(long arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public long getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(long departureTime) {
    this.departureTime = departureTime;
  }

  public static ArrivalAndDepartureTime getScheduledTime(
      BlockInstance blockInstance, BlockStopTimeEntry blockStopTime) {
    return getScheduledTime(blockInstance, blockStopTime, 0);
  }

  public static ArrivalAndDepartureTime getScheduledTime(
      BlockInstance blockInstance, BlockStopTimeEntry blockStopTime, int offset) {
    return getScheduledTime(blockInstance.getServiceDate(), blockStopTime,
        offset);
  }

  public static ArrivalAndDepartureTime getScheduledTime(
      StopTimeInstance stopTimeInstance) {
    return getScheduledTime(stopTimeInstance.getServiceDate(),
        stopTimeInstance.getStopTime(), 0);
  }

  public static ArrivalAndDepartureTime getScheduledTime(InstanceState state,
      BlockStopTimeEntry blockStopTime) {
    return getScheduledTime(state.getServiceDate(),blockStopTime, 0);
  }

  public static ArrivalAndDepartureTime getScheduledTime(long serviceDate,
      BlockStopTimeEntry blockStopTime, int offset) {
    if ( blockStopTime == null) {
      _log.error("blockStopTime is null");
      return null;
    }
    StopTimeEntry stopTime = blockStopTime.getStopTime();

    long arrivalTime = serviceDate + (stopTime.getArrivalTime() + offset)
        * 1000;
    long departureTime = serviceDate + (stopTime.getDepartureTime() + offset)
        * 1000;

    return new ArrivalAndDepartureTime(arrivalTime, departureTime);
  }

}
