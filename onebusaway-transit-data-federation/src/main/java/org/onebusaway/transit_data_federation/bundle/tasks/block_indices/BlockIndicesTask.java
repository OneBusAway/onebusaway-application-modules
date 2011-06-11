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
  private BlockIndexFactoryService _blockIndexFactoryService;
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
  public void setBlockIndexFactoryService(
      BlockIndexFactoryService blockIndexFactoryService) {
    _blockIndexFactoryService = blockIndexFactoryService;
  }

  @Autowired
  public void setRefreshService(RefreshService refreshService) {
    _refreshService = refreshService;
  }

  @Override
  public void run() {

    try {

      Iterable<BlockEntry> blocks = _transitGraphDao.getAllBlocks();

      List<BlockTripIndexData> tripData = _blockIndexFactoryService.createTripData(blocks);
      List<BlockLayoverIndexData> layoverData = _blockIndexFactoryService.createLayoverData(blocks);
      List<FrequencyBlockTripIndexData> frequencyTripData = _blockIndexFactoryService.createFrequencyTripData(blocks);

      ObjectSerializationLibrary.writeObject(_bundle.getBlockTripIndicesPath(),
          tripData);
      ObjectSerializationLibrary.writeObject(
          _bundle.getBlockLayoverIndicesPath(), layoverData);
      ObjectSerializationLibrary.writeObject(
          _bundle.getFrequencyBlockTripIndicesPath(), frequencyTripData);

      BlockStopTimeIndicesFactory stopFactory = new BlockStopTimeIndicesFactory();
      stopFactory.createIndices(blocks);

      _refreshService.refresh(RefreshableResources.BLOCK_INDEX_DATA);

    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }
}
