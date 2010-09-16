package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockGeospatialService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndex;
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
  public Set<BlockInstance> getActiveScheduledBlocksPassingThroughBounds(
      CoordinateBounds bounds, long timeFrom, long timeTo) {

    List<StopEntry> stops = _transitGraphDao.getStopsByLocation(bounds);

    Set<AgencyAndId> routeIds = new HashSet<AgencyAndId>();

    for (StopEntry stop : stops) {
      StopTimeIndex stopTimeIndex = stop.getStopTimes();
      for (LocalizedServiceId serviceId : stopTimeIndex.getServiceIds()) {
        List<StopTimeEntry> stopTimes = stopTimeIndex.getStopTimesForServiceIdSortedByArrival(serviceId);
        for (StopTimeEntry stopTime : stopTimes) {
          routeIds.add(stopTime.getTrip().getRouteCollectionId());
        }
      }
    }

    Set<BlockInstance> allBlockInstances = new HashSet<BlockInstance>();

    for (AgencyAndId routeId : routeIds) {
      List<BlockInstance> blockInstances = _blockCalendarService.getActiveBlocksForRouteInTimeRange(
          routeId, timeFrom, timeTo);
      allBlockInstances.addAll(blockInstances);
    }

    return allBlockInstances;
  }
}
