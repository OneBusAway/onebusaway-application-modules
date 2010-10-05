package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockGeospatialService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class BlockGeospatialServiceImpl implements BlockGeospatialService {

  private TransitGraphDao _transitGraphDao;

  private BlockCalendarService _blockCalendarService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
  }

  @Override
  public List<BlockInstance> getActiveScheduledBlocksPassingThroughBounds(
      CoordinateBounds bounds, long timeFrom, long timeTo) {

    List<StopEntry> stops = _transitGraphDao.getStopsByLocation(bounds);

    Set<BlockIndex> blockIndices = new HashSet<BlockIndex>();

    for (StopEntry stop : stops) {

      List<BlockStopTimeIndex> stopTimeIndices = stop.getStopTimeIndices();
      for (BlockStopTimeIndex stopTimeIndex : stopTimeIndices)
        blockIndices.add(stopTimeIndex.getBlockIndex());
    }

    return _blockCalendarService.getActiveBlocksInTimeRange(blockIndices,
        timeFrom, timeTo);
  }
}
