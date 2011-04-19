package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class TransitGraphTask implements Runnable {

  private FederatedTransitDataBundle _bundle;

  private StopEntriesFactory _stopEntriesFactory;

  private TripEntriesFactory _tripEntriesFactory;

  private BlockEntriesFactory _blockEntriesFactory;

  private RefreshService _refreshService;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
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

  @Autowired
  public void setRefreshService(RefreshService refreshService) {
    _refreshService = refreshService;
  }

  @Transactional
  public void run() {

    TransitGraphImpl graph = new TransitGraphImpl();

    _stopEntriesFactory.processStops(graph);
    _tripEntriesFactory.processTrips(graph);
    _blockEntriesFactory.processBlocks(graph);

    /**
     * Make sure the graph is initialized as result of the graph building
     * process, as it will be used by subsequent tasks
     */
    graph.initialize();

    try {

      ObjectSerializationLibrary.writeObject(_bundle.getTransitGraphPath(),
          graph);

    } catch (Exception ex) {
      throw new IllegalStateException("error writing graph to file", ex);
    }

    _refreshService.refresh(RefreshableResources.TRANSIT_GRAPH);

  }
}
