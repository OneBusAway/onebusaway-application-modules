package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockGeospatialService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStatusService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
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
  public Map<BlockInstance, List<BlockLocation>> getBlocks(AgencyAndId blockId,
      long serviceDate, AgencyAndId vehicleId, long time) {

    List<BlockInstance> blockInstances = getBlockInstances(blockId,
        serviceDate, time);

    Map<BlockInstance, List<BlockLocation>> results = new HashMap<BlockInstance, List<BlockLocation>>();

    for (BlockInstance blockInstance : blockInstances) {
      List<BlockLocation> locations = new ArrayList<BlockLocation>();
      computeLocations(blockInstance, vehicleId, time, locations);
      results.put(blockInstance, locations);
    }

    return results;
  }

  @Override
  public BlockLocation getBlockForVehicle(AgencyAndId vehicleId, long time) {
    TargetTime target = new TargetTime(time, time);
    return _blockLocationService.getLocationForVehicleAndTime(vehicleId, target);
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

  private List<BlockInstance> getBlockInstances(AgencyAndId blockId,
      long serviceDate, long time) {

    if (serviceDate != 0) {
      BlockInstance blockInstance = _blockCalendarService.getBlockInstance(
          blockId, serviceDate);
      if (blockInstance == null)
        return Collections.emptyList();
      BlockConfigurationEntry blockConfig = blockInstance.getBlock();
      if (blockConfig.getFrequencies() == null)
        return Arrays.asList(blockInstance);

      List<BlockInstance> instances = new ArrayList<BlockInstance>();
      for (FrequencyEntry frequency : blockConfig.getFrequencies())
        instances.add(new BlockInstance(blockConfig,
            blockInstance.getServiceDate(), frequency));
      return instances;
    } else {

      List<BlockInstance> instances = _blockCalendarService.getActiveBlocks(
          blockId, time, time);

      if (instances.isEmpty()) {
        instances = _blockCalendarService.getClosestActiveBlocks(blockId, time);
      }

      return instances;
    }
  }

  private List<BlockLocation> getAsLocations(Iterable<BlockInstance> instances,
      long time) {
    List<BlockLocation> locations = new ArrayList<BlockLocation>();
    for (BlockInstance instance : instances)
      computeLocations(instance, null, time, locations);
    return locations;
  }

  /**
   * 
   * @param instance
   * @param vehicleId optional filter on location results. Can be null.
   * @param time
   * @param results
   */
  private void computeLocations(BlockInstance instance, AgencyAndId vehicleId,
      long time, List<BlockLocation> results) {

    if (instance == null)
      return;

    TargetTime target = new TargetTime(time, time);

    // Try real-time trips first
    List<BlockLocation> locations = _blockLocationService.getLocationsForBlockInstance(
        instance, target);

    if (!locations.isEmpty()) {

      if (vehicleId == null) {
        results.addAll(locations);
      } else {
        for (BlockLocation location : locations)
          if (vehicleId.equals(location.getVehicleId()))
            results.add(location);
      }

    } else {

      // If no real-time trips are available and no vehicle id was specified,
      // use scheduled trips
      if (vehicleId == null) {
        BlockLocation location = _blockLocationService.getScheduledLocationForBlockInstance(
            instance, time);

        if (location != null && location.isInService())
          results.add(location);
      }
    }
  }
}
