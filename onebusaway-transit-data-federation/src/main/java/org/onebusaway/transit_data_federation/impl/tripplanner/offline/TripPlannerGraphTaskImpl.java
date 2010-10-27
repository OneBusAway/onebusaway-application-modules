package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class TripPlannerGraphTaskImpl implements Runnable {

  private FederatedTransitDataBundle _bundle;

  private TripPlannerGraphImpl _graph;

  private StopEntriesFactory _stopEntriesFactory;

  private TripEntriesFactory _tripEntriesFactory;

  private BlockEntriesFactory _blockEntriesFactory;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  public void setGraph(TripPlannerGraphImpl graph) {
    _graph = graph;
  }

  @Autowired
  public void setStopEntriesFactory(StopEntriesFactory stopEntriesFactory) {
    _stopEntriesFactory = stopEntriesFactory;
  }

  @Autowired
  public void setTripEntriesFactory(TripEntriesFactory tripEntriesFactory) {
    _tripEntriesFactory = tripEntriesFactory;
  }

  @Autowired
  public void setBlockEntriesFactory(BlockEntriesFactory blockEntriesFactory) {
    _blockEntriesFactory = blockEntriesFactory;
  }

  @Transactional
  public void run() {

    _stopEntriesFactory.processStops(_graph);
    _tripEntriesFactory.processTrips(_graph);
    _blockEntriesFactory.processBlocks(_graph);

    /**
     * Make sure the graph is initialized as result of the graph building
     * process, as it will be used by subsequent tasks
     */
    _graph.initialize();

    try {
      ObjectSerializationLibrary.writeObject(_bundle.getTripPlannerGraphPath(),
          _graph);
    } catch (Exception ex) {
      throw new IllegalStateException("error writing graph to file", ex);
    }
  }
}
