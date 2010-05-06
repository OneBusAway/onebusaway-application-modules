package org.onebusaway.tripplanner.impl.state;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.tripplanner.impl.TripPlannerStateTransition;
import org.onebusaway.tripplanner.model.StartState;
import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.TripPlannerGraph;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.WaitingAtStopState;

import java.util.List;
import java.util.Set;

public class StartStateHandler implements TripPlannerStateTransition {

  public void getTransitions(TripContext context, TripState state,
      Set<TripState> transitions) {

    TripPlannerConstants constants = context.getConstants();

    StartState start = (StartState) state;
    Point location = start.getLocation();
    Geometry boundary = location.buffer(5280 * 1 / 2).getBoundary();

    TripPlannerGraph graph = context.getGraph();
    List<Stop> stops = graph.getStopsByLocation(boundary);

    for (Stop stop : stops) {
      double d = stop.getLocation().distance(location);

      double walkingTime = d / constants.getWalkingVelocity();
      long t = (long) (state.getCurrentTime() + walkingTime + constants.getMinTransferTime());
      transitions.add(new WaitingAtStopState(t, stop));
    }
  }

}
