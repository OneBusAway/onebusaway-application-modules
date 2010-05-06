package org.onebusaway.tripplanner.impl.state;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.tripplanner.impl.TripPlannerStateTransition;
import org.onebusaway.tripplanner.model.StopEntry;
import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.TripPlannerGraph;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.WaitingAtStopState;
import org.onebusaway.tripplanner.model.WalkToAnotherStopState;

import java.util.List;
import java.util.Set;

public class WalkToAnotherStopStateHandler implements
    TripPlannerStateTransition {

  public void getTransitions(TripContext context, TripState state,
      Set<TripState> transitions) {

    WalkToAnotherStopState walk = (WalkToAnotherStopState) state;
    Stop stop = walk.getStop();
    Point location = stop.getLocation();
    
    TripPlannerGraph graph = context.getGraph();
    TripPlannerConstants constants = context.getConstants();
    
    /*
    
    double maxTransfer = constants.getMaxTransferDistance();
    Geometry boundary = location.buffer(maxTransfer).getBoundary();
    List<Stop> stops = graph.getStopsByLocation(boundary);
    */
    
    StopEntry entry = graph.getStopEntryByStopId(stop.getId());
    
    for( String id : entry.getTransfers() ) {

      if( id.equals(stop.getId()))
        continue;
      StopEntry nearbyEntry = graph.getStopEntryByStopId(id);
      Stop nearbyStop = nearbyEntry.getStop();
      
      double d = nearbyStop.getLocation().distance(location);
      double walkingTime = d / constants.getWalkingVelocity();
      long t = (long) (state.getCurrentTime() + walkingTime);
      transitions.add(new WaitingAtStopState(t, nearbyStop));
    }
  }
}
