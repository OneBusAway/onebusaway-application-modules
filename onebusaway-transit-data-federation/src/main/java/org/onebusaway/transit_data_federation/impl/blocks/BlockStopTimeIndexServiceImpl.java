package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.List;

import javax.annotation.PostConstruct;

import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopEntryImpl;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndexService;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockStopTimeIndexServiceImpl implements
    BlockStopTimeIndexService, Runnable {

  private TransitGraphDao _graphDao;

  private BlockIndexService _blockIndexService;

  @Autowired
  public void setGraphDao(TransitGraphDao graphDao) {
    _graphDao = graphDao;
  }

  @Autowired
  public void setBlockIndicesService(BlockIndexService blockIndexService) {
    _blockIndexService = blockIndexService;
  }

  @PostConstruct
  public void setup() {

    List<BlockIndex> blockIndices = _blockIndexService.getBlockIndices();

    // Clear any existing indices
    for (StopEntry stop : _graphDao.getAllStops()) {
      StopEntryImpl stopImpl = (StopEntryImpl) stop;
      stopImpl.getStopTimeIndices().clear();
    }

    for (BlockIndex blockIndex : blockIndices) {

      List<BlockConfigurationEntry> blocks = blockIndex.getBlocks();

      BlockConfigurationEntry firstBlock = blocks.get(0);
      BlockConfigurationEntry lastBlock = blocks.get(blocks.size() - 1);

      List<BlockStopTimeEntry> firstStopTimes = firstBlock.getStopTimes();
      List<BlockStopTimeEntry> lastStopTimes = lastBlock.getStopTimes();

      int n = firstStopTimes.size();

      for (int i = 0; i < n; i++) {

        BlockStopTimeEntry firstStopTime = firstStopTimes.get(i);
        BlockStopTimeEntry lastStopTime = lastStopTimes.get(i);

        ServiceInterval serviceInterval = getStopTimesAsServiceInterval(
            firstStopTime, lastStopTime);

        BlockStopTimeIndex blockStopTimeIndex = new BlockStopTimeIndex(
            blockIndex, i, serviceInterval);

        StopEntryImpl stop = (StopEntryImpl) firstStopTime.getStopTime().getStop();
        stop.addStopTimeIndex(blockStopTimeIndex);
      }
    }
  }

  /****
   * {@link Runnable} Interface
   ****/

  @Override
  public void run() {
    setup();
  }

  /****
   * {@link BlockStopTimeIndexService} Interface
   ****/

  @Override
  public List<BlockStopTimeIndex> getStopTimeIndicesForStop(StopEntry stopEntry) {
    return ((StopEntryImpl) stopEntry).getStopTimeIndices();
  }

  /****
   * Private Methods
   ****/

  private ServiceInterval getStopTimesAsServiceInterval(
      BlockStopTimeEntry firstStopTime, BlockStopTimeEntry lastStopTime) {

    StopTimeEntry st0 = firstStopTime.getStopTime();
    StopTimeEntry st1 = lastStopTime.getStopTime();

    return new ServiceInterval(st0.getArrivalTime(), st0.getDepartureTime(),
        st1.getArrivalTime(), st1.getDepartureTime());
  }

}
