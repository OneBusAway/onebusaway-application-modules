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
package org.onebusaway.transit_data_federation.impl.blocks;

import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

public class ScheduledBlockLocationLibrary {

  public static int computeTravelTimeBetweenLocations(
      ScheduledBlockLocation scheduledBlockLocationA,
      ScheduledBlockLocation scheduledBlockLocationB) {
    
    if( scheduledBlockLocationA.getScheduledTime() == scheduledBlockLocationB.getScheduledTime() )
      return 0;

    boolean inOrder = scheduledBlockLocationA.getScheduledTime() <= scheduledBlockLocationB.getScheduledTime();

    ScheduledBlockLocation from = inOrder ? scheduledBlockLocationA
        : scheduledBlockLocationB;
    ScheduledBlockLocation to = inOrder ? scheduledBlockLocationB
        : scheduledBlockLocationA;

    int delta = to.getScheduledTime() - from.getScheduledTime();

    BlockStopTimeEntry fromStop = from.getNextStop();
    BlockStopTimeEntry toStop = to.getNextStop();

    int slack = toStop.getAccumulatedSlackTime()
        - fromStop.getAccumulatedSlackTime();

    int slackFrom = computeSlackToNextStop(from);
    slack += slackFrom;
    int slackTo = computeSlackToNextStop(to);
    slack -= slackTo;

    return Math.max(0, delta - slack);
  }

  public static int computeSlackToNextStop(
      ScheduledBlockLocation scheduledBlockLocation) {

    BlockStopTimeEntry nextStop = scheduledBlockLocation.getNextStop();
    StopTimeEntry stopTime = nextStop.getStopTime();
    int t = scheduledBlockLocation.getScheduledTime();

    /**
     * If we are actually at the next stop already, we return a negative value:
     * the amount of slack time already consumed.
     */
    if (stopTime.getArrivalTime() <= t && t <= stopTime.getDepartureTime())
      return stopTime.getArrivalTime() - t;

    int sequence = nextStop.getBlockSequence();

    /**
     * Are we before the first stop in the block? Are we already at the stop or
     * on our way there? Not sure, for now, let's assume there is no slack to be
     * had
     */
    if (sequence == 0)
      return 0;

    BlockConfigurationEntry blockConfig = nextStop.getTrip().getBlockConfiguration();
    BlockStopTimeEntry previousStop = blockConfig.getStopTimes().get(
        sequence - 1);

    int slack = nextStop.getAccumulatedSlackTime()
        - previousStop.getAccumulatedSlackTime();
    slack -= previousStop.getStopTime().getSlackTime();

    int timeToNextStop = stopTime.getArrivalTime() - t;

    if (timeToNextStop > slack)
      return slack;
    else
      return timeToNextStop;
  }
}
