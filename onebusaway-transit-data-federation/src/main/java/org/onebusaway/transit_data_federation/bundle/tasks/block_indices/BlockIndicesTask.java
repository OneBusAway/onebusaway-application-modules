package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.util.List;

import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripPlannerGraphImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripPlannerGraphRefresh;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.springframework.beans.factory.annotation.Autowired;

public class BlockIndicesTask implements Runnable {

  private FederatedTransitDataBundle _bundle;
  private TripPlannerGraphImpl _graph;
  private TripPlannerGraphRefresh _refresh;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  public void setGraph(TripPlannerGraphImpl graph) {
    _graph = graph;
  }

  @Autowired
  public void setRefresh(TripPlannerGraphRefresh refresh) {
    _refresh = refresh;
  }

  @Override
  public void run() {

    try {

      _refresh.refresh();

      BlockIndicesFactory factory = new BlockIndicesFactory();
      factory.setVerbose(true);

      Iterable<BlockEntry> blocks = _graph.getAllBlocks();

      List<BlockIndexData> data = factory.createData(blocks);

      ObjectSerializationLibrary.writeObject(_bundle.getBlockIndicesPath(),
          data);
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }
}
