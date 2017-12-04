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
package org.onebusaway.transit_data_federation.services;

import java.util.List;

import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.model.StopTimeInstance;

/**
 * Service methods for determining the set of active stop times at a particular
 * stop and time.
 * 
 * @author bdferris
 * @see StopTimeInstance
 */
public interface ArrivalAndDepartureService {

  /**
   * Determines the set of active arrivals and departures at a given stop,
   * taking into account real-time arrival information.
   * 
   */
  public List<ArrivalAndDepartureInstance> getArrivalsAndDeparturesForStopInTimeRange(
      StopEntry stop, TargetTime targetTime, long fromTime, long toTime);

  /**
   * Determines the set of active arrivals and departures at a given stop, NOT
   * taking into account real-time arrival information.
   * 
   */
  public List<ArrivalAndDepartureInstance> getScheduledArrivalsAndDeparturesForStopInTimeRange(
      StopEntry stop, long currentTime, long fromTime, long toTime);

  /**
   * 
   * @param stop
   * @param time
   * @param includePrivateService TODO
   * @return
   */
  public List<ArrivalAndDepartureInstance> getNextScheduledBlockTripDeparturesForStop(
      StopEntry stop, long time, boolean includePrivateService);

  public ArrivalAndDepartureInstance getArrivalAndDepartureForStop(
      ArrivalAndDepartureQuery query);

  /**
   * Given an arrival and departure instance, compute the arrival and departure
   * instance for the previous stop along the block. If at the start of the
   * block, this method will return null.
   * 
   * @param instance
   * @return
   */
  public ArrivalAndDepartureInstance getPreviousStopArrivalAndDeparture(
      ArrivalAndDepartureInstance instance);

  /**
   * Given an arrival and departure instance, compute the arrival and departure
   * instance for the next stop along the block. If at the end of the block,
   * this method will return null.
   * 
   * @param instance
   * @return
   */
  public ArrivalAndDepartureInstance getNextStopArrivalAndDeparture(
      ArrivalAndDepartureInstance instance);
}
