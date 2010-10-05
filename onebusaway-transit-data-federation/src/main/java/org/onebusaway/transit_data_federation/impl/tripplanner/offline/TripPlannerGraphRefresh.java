package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.springframework.beans.factory.annotation.Autowired;

public class TripPlannerGraphRefresh {

  private TripPlannerGraphImpl _graph;

  private FederatedTransitDataBundle _bundle;

  @Autowired
  public void setGraph(TripPlannerGraphImpl graph) {
    _graph = graph;
  }

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  public void refresh() {
    try {
      if (_graph.getAllStops().iterator().hasNext()) {
        _graph.initialize();
        return;
      }
      TripPlannerGraphImpl newGraph = ObjectSerializationLibrary.readObject(_bundle.getTripPlannerGraphPath());
      _graph.initializeFromExistinGraph(newGraph);
    } catch (Exception ex) {
      throw new IllegalStateException("Error loading graph", ex);
    }

  }
}
