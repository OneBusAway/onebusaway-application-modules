package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockGeospatialService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStatusService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockStatusServiceImpl implements BlockStatusService {

  /**
   * Catch late trips up to 30 minutes
   */
  private static final long TIME_BEFORE_WINDOW = 30 * 60 * 1000;

  /**
   * Catch early blocks up to 10 minutes
   */
  private static final long TIME_AFTER_WINDOW = 10 * 60 * 1000;

  private BlockCalendarService _blockCalendarService;

  private BlockLocationService _blockLocationService;

  private BlockGeospatialService _blockGeospatialService;

  @Autowired
  public void setActive(BlockCalendarService activeCalendarService) {
    _blockCalendarService = activeCalendarService;
  }

  @Autowired
  public void setBlockLocationService(BlockLocationService blockLocationService) {
    _blockLocationService = blockLocationService;
  }

  @Autowired
  public void setBlockGeospatialService(
      BlockGeospatialService blockGeospatialService) {
    _blockGeospatialService = blockGeospatialService;
  }

  /****
   * {@link BlockStatusService} Interface
   ****/

  @Override
  public BlockLocation getBlock(AgencyAndId blockId, long serviceDate, long time) {

    BlockInstance blockInstance = _blockCalendarService.getActiveBlock(blockId,
        serviceDate, time);
    return getLocation(blockInstance, time);
  }

  @Override
  public BlockLocation getBlockForVehicle(AgencyAndId vehicleId, long time) {
    return _blockLocationService.getLocationForVehicleAndTime(vehicleId, time);
  }

  @Override
  public List<BlockLocation> getBlocksForAgency(String agencyId, long time) {

    List<BlockInstance> instances = _blockCalendarService.getActiveBlocksForAgencyInTimeRange(
        agencyId, time, time);

    return getAsLocations(instances, time);
  }

  @Override
  public List<BlockLocation> getBlocksForRoute(AgencyAndId routeId, long time) {

    List<BlockInstance> instances = _blockCalendarService.getActiveBlocksForRouteInTimeRange(
        routeId, time, time);

    return getAsLocations(instances, time);
  }

  @Override
  public List<BlockLocation> getBlocksForBounds(CoordinateBounds bounds,
      long time) {

    long timeFrom = time - TIME_BEFORE_WINDOW;
    long timeTo = time + TIME_AFTER_WINDOW;

    Set<BlockInstance> instances = _blockGeospatialService.getActiveScheduledBlocksPassingThroughBounds(
        bounds, timeFrom, timeTo);

    List<BlockLocation> locations = getAsLocations(instances, time);
    List<BlockLocation> inRange = new ArrayList<BlockLocation>();
    for (BlockLocation location : locations) {
      CoordinatePoint p = location.getLocation();
      if (bounds.contains(p))
        inRange.add(location);
    }

    return inRange;
  }

  /****
   * Private Methods
   ****/

  private List<BlockLocation> getAsLocations(Iterable<BlockInstance> instances,
      long time) {
    List<BlockLocation> locations = new ArrayList<BlockLocation>();
    for (BlockInstance instance : instances) {
      BlockLocation location = getLocation(instance, time);
      if (location != null)
        locations.add(location);
    }
    return locations;
  }

  private BlockLocation getLocation(BlockInstance instance, long time) {
    if (instance == null)
      return null;
    return _blockLocationService.getPositionForBlockInstance(instance, time);
  }
}
