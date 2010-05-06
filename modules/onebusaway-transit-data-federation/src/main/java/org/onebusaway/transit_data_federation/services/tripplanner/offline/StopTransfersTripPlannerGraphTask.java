package org.onebusaway.transit_data_federation.services.tripplanner.offline;

import org.onebusaway.transit_data_federation.services.RunnableWithOutputPath;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerGraph;

public interface StopTransfersTripPlannerGraphTask extends
    RunnableWithOutputPath {

  public void setWalkPlannerGraph(WalkPlannerGraph graph);

  public void setTripPlannerGraph(TripPlannerGraph tripPlannerGraph);
}
