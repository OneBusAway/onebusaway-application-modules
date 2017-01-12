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
package org.onebusaway.transit_data_federation.impl.otp.graph;

import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.ItineraryWeightingLibrary;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateEditor;
import org.onebusaway.transit_data_federation.impl.otp.OBATraverseOptions;
import org.onebusaway.transit_data_federation.impl.otp.SupportLibrary;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.utility.time.SystemTime;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;

public class DepartureEdge extends AbstractEdge {

  private final StopEntry _stop;

  public DepartureEdge(GraphContext context, StopEntry stop) {
    super(context);
    _stop = stop;
  }

  @Override
  public State traverse(State s0) {
    TraverseOptions options = s0.getOptions();
    if (options.isArriveBy())
      return traverseReverse(s0);
    else
      return traverseForward(s0);
  }

  private State traverseForward(State s0) {

    OBATraverseOptions obaOpts = (OBATraverseOptions) s0.getOptions();
    if (obaOpts.extraSpecialMode)
      return getNextScheduledBlockDepartureResults(s0);

    State results = null;

    long time = s0.getTime();

    /**
     * Look for departures in the next X minutes
     */
    long fromTime = time;
    long toTime = SupportLibrary.getNextTimeWindow(_context, time);

    List<ArrivalAndDepartureInstance> departures = getDeparturesInTimeRange(s0,
        fromTime, toTime);

    for (ArrivalAndDepartureInstance instance : departures) {

      long departureTime = instance.getBestDepartureTime();

      /**
       * Prune anything that doesn't have a departure in the proper range, since
       * the arrivals and departures method will also return instances that
       * arrive in the target interval as well
       */
      if (departureTime < time || toTime <= departureTime)
        continue;

      // If this is the last stop time in the block, don't continue
      if (!SupportLibrary.hasNextStopTime(instance))
        continue;

      State r = getDepartureAsTraverseResult(instance, s0);
      results = r.addToExistingResultChain(results);
    }

    // In addition to all the departures, we can just remain waiting at the stop
    DepartureVertex fromVertex = new DepartureVertex(_context, _stop,
        s0.getTime());
    DepartureVertex toVertex = new DepartureVertex(_context, _stop, toTime);
    EdgeNarrative narrative = narrative(s0, fromVertex, toVertex);

    OBAStateEditor edit = (OBAStateEditor) s0.edit(this, narrative);
    edit.setTime(toTime);

    int dwellTime = (int) ((toTime - time) / 1000);
    double w = ItineraryWeightingLibrary.computeWeightForWait(s0, dwellTime);
    edit.incrementWeight(w);

    if (s0.getNumBoardings() == 0)
      edit.incrementInitialWaitTime(dwellTime * 1000);

    State r = edit.makeState();
    results = r.addToExistingResultChain(results);

    return results;
  }

  private State traverseReverse(State s0) {

    DepartureVertex fromVertex = new DepartureVertex(_context, _stop,
        s0.getTime());
    Vertex toVertex = null;
    EdgeNarrative narrative = narrative(s0, fromVertex, toVertex);

    return s0.edit(this, narrative).makeState();
  }

  /****
   * Private Methods
   ****/

  private List<ArrivalAndDepartureInstance> getDeparturesInTimeRange(State s0,
      long fromTime, long toTime) {

    boolean useRealtime = false;
    TraverseOptions options = s0.getOptions();
    OBATraverseOptions config = options.getExtension(OBATraverseOptions.class);
    if (config != null)
      useRealtime = config.useRealtime;

    ArrivalAndDepartureService service = _context.getArrivalAndDepartureService();

    if (useRealtime) {
      /**
       * TODO : If we want to simulate real-time trip planning with the system
       * in some past state, we'll need a way to adjust NOW here
       */
      TargetTime time = new TargetTime(s0.getTime(), SystemTime.currentTimeMillis());
      return service.getArrivalsAndDeparturesForStopInTimeRange(_stop, time,
          fromTime, toTime);
    } else {
      return service.getScheduledArrivalsAndDeparturesForStopInTimeRange(_stop,
          s0.getTime(), fromTime, toTime);
    }
  }

  private State getDepartureAsTraverseResult(
      ArrivalAndDepartureInstance instance, State s0) {

    long time = s0.getTime();

    long departureTime = instance.getBestDepartureTime();

    DepartureVertex fromVertex = new DepartureVertex(_context, _stop, time);
    BlockDepartureVertex toVertex = new BlockDepartureVertex(_context, instance);
    EdgeNarrative narrative = narrative(s0, fromVertex, toVertex);

    OBAStateEditor edit = (OBAStateEditor) s0.edit(this, narrative);
    edit.setTime(departureTime);
    edit.incrementNumBoardings();
    edit.setEverBoarded(true);

    int dwellTime = (int) ((departureTime - time) / 1000);
    double w = ItineraryWeightingLibrary.computeWeightForWait(s0, dwellTime);

    if (s0.getNumBoardings() == 0)
      edit.incrementInitialWaitTime(dwellTime * 1000);

    edit.appendTripSequence(instance.getBlockTrip());
    edit.incrementWeight(w);

    return edit.makeState();
  }

  private State getNextScheduledBlockDepartureResults(State s0) {

    ArrivalAndDepartureService arrivalAndDepartureService = _context.getArrivalAndDepartureService();

    List<ArrivalAndDepartureInstance> instances = arrivalAndDepartureService.getNextScheduledBlockTripDeparturesForStop(
        _stop, s0.getTime(), false);

    State results = null;

    for (ArrivalAndDepartureInstance instance : instances) {
      State r = getDepartureAsTraverseResult(instance, s0);
      results = r.addToExistingResultChain(results);
    }

    return results;
  }
}
