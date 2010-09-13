package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.GtfsServiceBundle;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.springframework.beans.factory.annotation.Autowired;

public class BlockIndicesTask implements Runnable {

  private FederatedTransitDataBundle _bundle;

  private CalendarService _calendarService;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Override
  public void run() {

    try {

      GtfsServiceBundle bundle = new GtfsServiceBundle(_bundle.getPath());
      File calendarServiceDataPath = bundle.getCalendarServiceDataPath();
      if (calendarServiceDataPath.exists()) {
        CalendarServiceData data = ObjectSerializationLibrary.readObject(calendarServiceDataPath);
        CalendarServiceImpl calendarService = new CalendarServiceImpl();
        calendarService.setData(data);
        _calendarService = calendarService;
      }

      TripPlannerGraph graph = ObjectSerializationLibrary.readObject(_bundle.getTripPlannerGraphPath());

      BlockIndicesFactory factory = new BlockIndicesFactory();
      factory.setCalendarService(_calendarService);
      factory.setVerbose(true);

      Iterable<BlockEntry> blocks = graph.getAllBlocks();

      /****
       * By Agency Id
       ****/

      Map<String, List<BlockEntry>> blocksByAgencyId = getBlocksByAgencyId(blocks);

      Map<String, List<BlockIndexData>> dataByAgencyId = computeData(factory,
          blocksByAgencyId);

      ObjectSerializationLibrary.writeObject(
          _bundle.getBlockIndicesByAgencyPath(), dataByAgencyId);

      /****
       * By Route Id
       ****/

      Map<AgencyAndId, List<BlockEntry>> blocksByRouteId = getBlocksByRouteId(blocks);

      Map<AgencyAndId, List<BlockIndexData>> dataByRouteId = computeData(
          factory, blocksByRouteId);

      ObjectSerializationLibrary.writeObject(
          _bundle.getBlockIndicesByRoutePath(), dataByRouteId);

    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  private Map<String, List<BlockEntry>> getBlocksByAgencyId(
      Iterable<BlockEntry> blocks) {

    Map<String, List<BlockEntry>> blocksByAgencyId = new FactoryMap<String, List<BlockEntry>>(
        new ArrayList<BlockEntry>());

    for (BlockEntry block : blocks) {
      Set<String> agencyIds = new HashSet<String>();
      for (TripEntry trip : block.getTrips())
        agencyIds.add(trip.getId().getAgencyId());
      for (String agencyId : agencyIds)
        blocksByAgencyId.get(agencyId).add(block);
    }
    return blocksByAgencyId;
  }

  private Map<AgencyAndId, List<BlockEntry>> getBlocksByRouteId(
      Iterable<BlockEntry> blocks) {

    Map<AgencyAndId, List<BlockEntry>> blocksByRouteId = new FactoryMap<AgencyAndId, List<BlockEntry>>(
        new ArrayList<BlockEntry>());

    for (BlockEntry block : blocks) {
      Set<AgencyAndId> routeIds = new HashSet<AgencyAndId>();
      for (TripEntry trip : block.getTrips())
        routeIds.add(trip.getRouteCollectionId());
      for (AgencyAndId routeId : routeIds)
        blocksByRouteId.get(routeId).add(block);
    }
    return blocksByRouteId;
  }

  private <T> Map<T, List<BlockIndexData>> computeData(
      BlockIndicesFactory factory, Map<T, List<BlockEntry>> blocksByRouteId) {

    Map<T, List<BlockIndexData>> dataByRouteId = new HashMap<T, List<BlockIndexData>>();

    for (Map.Entry<T, List<BlockEntry>> entry : blocksByRouteId.entrySet()) {
      T key = entry.getKey();
      List<BlockEntry> blocksForRoute = entry.getValue();
      List<BlockIndexData> dataForRoute = factory.createData(blocksForRoute);
      dataByRouteId.put(key, dataForRoute);
    }

    return dataByRouteId;
  }
}
