package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockGeospatialService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockTripIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class BlockGeospatialServiceImpl implements BlockGeospatialService {

  private TransitGraphDao _transitGraphDao;

  private BlockCalendarService _blockCalendarService;

  private BlockIndexService _blockIndexService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
  }

  @Autowired
  public void setBlockStopTimeIndexService(
      BlockIndexService blockStopTimeIndexService) {
    _blockIndexService = blockStopTimeIndexService;
  }

  @Override
  public List<BlockInstance> getActiveScheduledBlocksPassingThroughBounds(
      CoordinateBounds bounds, long timeFrom, long timeTo) {

    List<StopEntry> stops = _transitGraphDao.getStopsByLocation(bounds);

    Set<BlockTripIndex> blockIndices = new HashSet<BlockTripIndex>();

    for (StopEntry stop : stops) {

      List<BlockStopTimeIndex> stopTimeIndices = _blockIndexService.getStopTimeIndicesForStop(stop);
      for (BlockStopTimeIndex stopTimeIndex : stopTimeIndices)
        blockIndices.add(stopTimeIndex.getBlockIndex());
    }

    List<FrequencyBlockTripIndex> frequencyIndices = Collections.emptyList();

    return _blockCalendarService.getActiveBlocksInTimeRange(blockIndices,
        frequencyIndices, timeFrom, timeTo);
  }
}
