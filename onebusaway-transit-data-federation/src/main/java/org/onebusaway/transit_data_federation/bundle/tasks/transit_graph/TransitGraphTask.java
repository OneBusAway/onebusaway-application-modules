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
