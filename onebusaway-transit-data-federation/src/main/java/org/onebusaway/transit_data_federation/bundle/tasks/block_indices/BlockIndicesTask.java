package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.util.List;

import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.springframework.beans.factory.annotation.Autowired;

public class BlockIndicesTask implements Runnable {

  private FederatedTransitDataBundle _bundle;
  private TransitGraphDao _transitGraphDao;
  private RefreshService _refreshService;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setRefreshService(RefreshService refreshService) {
    _refreshService = refreshService;
  }

  @Override
  public void run() {

    try {

      BlockIndicesFactory factory = new BlockIndicesFactory();
      factory.setVerbose(true);

      Iterable<BlockEntry> blocks = _transitGraphDao.getAllBlocks();

      List<BlockTripIndexData> tripData = factory.createTripData(blocks);
      List<BlockLayoverIndexData> layoverData = factory.createLayoverData(blocks);
      List<FrequencyBlockTripIndexData> frequencyTripData = factory.createFrequencyTripData(blocks);

      ObjectSerializationLibrary.writeObject(_bundle.getBlockTripIndicesPath(),
          tripData);
      ObjectSerializationLibrary.writeObject(
          _bundle.getBlockLayoverIndicesPath(), layoverData);
      ObjectSerializationLibrary.writeObject(
          _bundle.getFrequencyBlockTripIndicesPath(), frequencyTripData);

      _refreshService.refresh(RefreshableResources.BLOCK_INDEX_DATA);

    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }
}
