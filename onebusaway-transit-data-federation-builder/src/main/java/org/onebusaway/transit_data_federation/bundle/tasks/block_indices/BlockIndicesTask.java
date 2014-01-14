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
package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.util.List;

import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.blocks.BlockStopTimeIndicesFactory;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexFactoryService;
import org.onebusaway.transit_data_federation.services.blocks.BlockLayoverIndexData;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndexData;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockTripIndexData;
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
