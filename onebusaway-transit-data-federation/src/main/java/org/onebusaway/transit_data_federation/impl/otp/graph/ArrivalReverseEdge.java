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
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;

public class ArrivalReverseEdge extends AbstractEdge {

  private final StopEntry _stop;

  public ArrivalReverseEdge(GraphContext context, StopEntry stop) {
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

    /**
     * We alight from our current vehicle to the stop. However, we don't
     * actually know which vehicle. Hopefully this method will only ever be
     * called in the GraphPath.optimize(), where the traverseBack() method has
     * previously been called.
     */
    Vertex fromVertex = null;
    Vertex toVertex = new ArrivalVertex(_context, _stop, s0.getTime());
    EdgeNarrative narrative = narrative(s0, fromVertex, toVertex);
    return s0.edit(this, narrative).makeState();
  }

  private State traverseReverse(State s0) {

    State results = null;

    long time = s0.getTime();
    TraverseOptions options = s0.getOptions();

    /**
     * Look for arrivals in the previous X minutes
     */
    long timeFrom = SupportLibrary.getPreviousTimeWindow(_context, time);
    long timeTo = time;

    List<ArrivalAndDepartureInstance> arrivals = getArrivalsInTimeRange(time,
        timeFrom, timeTo, options);

    for (ArrivalAndDepartureInstance instance : arrivals) {

      long arrivalTime = instance.getBestArrivalTime();

      // Prune anything that doesn't have an arrival time in the proper range,
      // since the stopTimeService method will also return instances that depart
      // in the target interval as well
      if (arrivalTime < timeFrom || time <= arrivalTime)
        continue;

      Vertex fromVertex = new BlockArrivalVertex(_context, instance);
      Vertex toVertex = new ArrivalVertex(_context, _stop, s0.getTime());
      EdgeNarrative narrative = narrative(s0, fromVertex, toVertex);

      OBAStateEditor edit = (OBAStateEditor) s0.edit(this, narrative);

      int dwellTime = (int) ((time - arrivalTime) / 1000);

      edit.setTime(arrivalTime);
      edit.incrementNumBoardings();
      edit.setEverBoarded(true);

      if (s0.getNumBoardings() == 0)
        edit.incrementInitialWaitTime(dwellTime * 1000);

      double w = ItineraryWeightingLibrary.computeWeightForWait(s0, dwellTime);
      edit.incrementWeight(w);

      State r = edit.makeState();
      results = r.addToExistingResultChain(results);
    }

    // In addition to all the departures, we can just remain waiting at the stop
    int dwellTime = (int) ((time - timeFrom) / 1000);
    double w = ItineraryWeightingLibrary.computeWeightForWait(s0, dwellTime);

    Vertex fromVertex = new ArrivalVertex(_context, _stop, timeFrom);
    Vertex toVertex = new ArrivalVertex(_context, _stop, s0.getTime());
    EdgeNarrative narrative = narrative(s0, fromVertex, toVertex);

    OBAStateEditor edit = (OBAStateEditor) s0.edit(this, narrative);

    edit.incrementWeight(w);
    edit.setTime(time);

    if (s0.getNumBoardings() == 0)
      edit.incrementInitialWaitTime(dwellTime * 1000);

    State r = edit.makeState();
    results = r.addToExistingResultChain(results);

    return results;
  }

  /****
   * Private Methods
   ****/

  private List<ArrivalAndDepartureInstance> getArrivalsInTimeRange(long time,
      long timeFrom, long timeTo, TraverseOptions options) {

    boolean useRealTime = false;
    OBATraverseOptions config = options.getExtension(OBATraverseOptions.class);
    if (config != null)
      useRealTime = config.useRealtime;

    ArrivalAndDepartureService service = _context.getArrivalAndDepartureService();

    if (useRealTime) {
      /**
       * TODO : If we want to simulate real-time trip planning with the system
       * in some past state, we'll need a way to adjust NOW here
       */
      TargetTime target = new TargetTime(time, System.currentTimeMillis());
      return service.getArrivalsAndDeparturesForStopInTimeRange(_stop, target,
          timeFrom, timeTo);
    } else {
      return service.getScheduledArrivalsAndDeparturesForStopInTimeRange(_stop,
          time, timeFrom, timeTo);
    }
  }
}
