package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

  private static final BlockLocationVehicleIdComparator _vehicleIdComparator = new BlockLocationVehicleIdComparator();

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

    List<BlockLocation> locations = getBlocks(blockId, serviceDate, time);

    if (locations.isEmpty())
      return null;
    else if (locations.size() == 1)
      return locations.get(0);

    Collections.sort(locations, _vehicleIdComparator);

    return locations.get(0);
  }

  @Override
  public List<BlockLocation> getBlocks(AgencyAndId blockId, long serviceDate,
      long time) {

    List<BlockInstance> blockInstances = _blockCalendarService.getActiveBlocks(
        blockId, time, time);

    List<BlockLocation> locations = new ArrayList<BlockLocation>();
    for (BlockInstance blockInstance : blockInstances) {
      long blockServiceDate = blockInstance.getServiceDate();
      // How should we handle this check?
      if (blockServiceDate != serviceDate)
        continue;
      computeLocations(blockInstance, time, locations);
    }
    return locations;
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

    List<BlockInstance> instances = _blockGeospatialService.getActiveScheduledBlocksPassingThroughBounds(
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
    for (BlockInstance instance : instances)
      computeLocations(instance, time, locations);
    return locations;
  }

  private void computeLocations(BlockInstance instance, long time,
      List<BlockLocation> results) {

    if (instance == null)
      return;

    // Try real-time trips first
    List<BlockLocation> locations = _blockLocationService.getLocationsForBlockInstance(
        instance, time);

    if (!locations.isEmpty()) {
      results.addAll(locations);
    } else {

      // If no real-time trips are available, use scheduled trips
      BlockLocation location = _blockLocationService.getScheduledLocationForBlockInstance(
          instance, time);

      if (location != null)
        results.add(location);
    }
  }

  /**
   * The block location with the first vehicle id should come first. If no
   * vehicle id is set, push it to the back.
   * 
   * @author bdferris
   * 
   */
  private static class BlockLocationVehicleIdComparator implements
      Comparator<BlockLocation> {

    @Override
    public int compare(BlockLocation o1, BlockLocation o2) {
      AgencyAndId v1 = o1.getVehicleId();
      AgencyAndId v2 = o2.getVehicleId();
      if (v1 == null)
        return v2 == null ? 0 : 1;
      if (v2 == null)
        return -1;
      return v1.compareTo(v2);
    }
  }
}
