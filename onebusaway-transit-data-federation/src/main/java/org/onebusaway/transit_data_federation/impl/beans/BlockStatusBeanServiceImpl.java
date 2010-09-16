package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.blocks.BlockDetailsBean;
import org.onebusaway.transit_data.model.blocks.BlockStatusBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.beans.BlockBeanService;
import org.onebusaway.transit_data_federation.services.beans.BlockStatusBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.realtime.ActiveCalendarService;
import org.onebusaway.transit_data_federation.services.realtime.BlockInstance;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndex;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockStatusBeanServiceImpl implements BlockStatusBeanService {

  /**
   * Catch late trips up to 30 minutes
   */
  private static final long TIME_BEFORE_WINDOW = 30 * 60 * 1000;

  /**
   * Catch early blocks up to 10 minutes
   */
  private static final long TIME_AFTER_WINDOW = 10 * 60 * 1000;

  private TransitGraphDao _graph;

  private ActiveCalendarService _activeCalendarService;

  private BlockLocationService _blockLocationService;

  private BlockBeanService _blockBeanService;

  private TripBeanService _tripBeanService;

  private StopBeanService _stopBeanService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao graph) {
    _graph = graph;
  }

  @Autowired
  public void setActive(ActiveCalendarService activeCalendarService) {
    _activeCalendarService = activeCalendarService;
  }

  @Autowired
  public void setBlockLocationService(BlockLocationService blockLocationService) {
    _blockLocationService = blockLocationService;
  }

  @Autowired
  public void setBlockBeanService(BlockBeanService blockBeanService) {
    _blockBeanService = blockBeanService;
  }

  @Autowired
  public void setTripBeanService(TripBeanService tripBeanService) {
    _tripBeanService = tripBeanService;
  }

  @Autowired
  public void setStopBeanService(StopBeanService stopBeanService) {
    _stopBeanService = stopBeanService;
  }

  /****
   * {@link BlockStatusBeanService} Interface
   ****/

  @Override
  public BlockDetailsBean getBlock(AgencyAndId blockId, long serviceDate,
      long time, TripDetailsInclusionBean inclusion) {

    BlockInstance blockInstance = _activeCalendarService.getActiveBlock(
        blockId, serviceDate, time);

    return getBlockIntanceAsBean(blockInstance, time, inclusion);
  }

  @Override
  public BlockDetailsBean getBlockForVehicle(AgencyAndId vehicleId, long time,
      TripDetailsInclusionBean inclusion) {

    BlockLocation blockLocation = _blockLocationService.getLocationForVehicleAndTime(
        vehicleId, time);

    if (blockLocation == null)
      return null;

    TripEntry trip = blockLocation.getActiveTrip();
    BlockEntry blockEntry = trip.getBlock();

    return getBlockAndLocationAsBean(blockEntry, blockLocation, inclusion);
  }

  @Override
  public ListBean<BlockDetailsBean> getBlocksForAgency(
      TripsForAgencyQueryBean query) {

    String agencyId = query.getAgencyId();
    long time = query.getTime();

    List<BlockInstance> instances = _activeCalendarService.getActiveBlocksForAgencyInTimeRange(
        agencyId, time, time);

    return getBlockInstancesAsBeans(instances, query.getTime(),
        query.getInclusion());
  }

  @Override
  public ListBean<BlockDetailsBean> getBlocksForRoute(
      TripsForRouteQueryBean query) {

    AgencyAndId routeId = AgencyAndIdLibrary.convertFromString(query.getRouteId());
    long time = query.getTime();
    TripDetailsInclusionBean inclusion = query.getInclusion();

    List<BlockInstance> instances = _activeCalendarService.getActiveBlocksForRouteInTimeRange(
        routeId, time, time);

    return getBlockInstancesAsBeans(instances, query.getTime(), inclusion);
  }

  @Override
  public ListBean<BlockDetailsBean> getBlocksForBounds(
      TripsForBoundsQueryBean query) {

    CoordinateBounds bounds = query.getBounds();
    long timeFrom = query.getTime() - TIME_BEFORE_WINDOW;
    long timeTo = query.getTime() + TIME_AFTER_WINDOW;

    List<StopEntry> stops = _graph.getStopsByLocation(bounds);

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
      List<BlockInstance> blockInstances = _activeCalendarService.getActiveBlocksForRouteInTimeRange(
          routeId, timeFrom, timeTo);
      allBlockInstances.addAll(blockInstances);
    }

    ListBean<BlockDetailsBean> beans = getBlockInstancesAsBeans(
        allBlockInstances, query.getTime(), query.getInclusion());

    List<BlockDetailsBean> beansInRange = new ArrayList<BlockDetailsBean>();

    for (BlockDetailsBean bean : beans.getList()) {
      BlockStatusBean status = bean.getStatus();
      CoordinatePoint location = status.getLocation();
      if (bounds.contains(location.getLat(), location.getLon()))
        beansInRange.add(bean);
    }

    return new ListBean<BlockDetailsBean>(beansInRange, beans.isLimitExceeded());
  }

  /****
   * Private Methods
   ****/

  private ListBean<BlockDetailsBean> getBlockInstancesAsBeans(
      Iterable<BlockInstance> instances, long time,
      TripDetailsInclusionBean inclusion) {

    List<BlockDetailsBean> results = new ArrayList<BlockDetailsBean>();

    for (BlockInstance instance : instances) {
      BlockDetailsBean details = getBlockIntanceAsBean(instance, time,
          inclusion);
      if (details != null)
        results.add(details);
    }

    return new ListBean<BlockDetailsBean>(results, false);
  }

  private BlockDetailsBean getBlockIntanceAsBean(BlockInstance instance,
      long time, TripDetailsInclusionBean inclusion) {

    BlockEntry blockEntry = instance.getBlock();

    BlockLocation blockLocation = _blockLocationService.getPositionForBlockInstance(
        instance, time);

    if (blockLocation == null)
      return null;

    return getBlockAndLocationAsBean(blockEntry, blockLocation, inclusion);
  }

  private BlockDetailsBean getBlockAndLocationAsBean(BlockEntry blockEntry,
      BlockLocation blockLocation, TripDetailsInclusionBean inclusion) {

    BlockDetailsBean details = new BlockDetailsBean();

    details.setBlockId(AgencyAndIdLibrary.convertToString(blockEntry.getId()));

    if (inclusion.isIncludeTripBean())
      details.setBlock(_blockBeanService.getBlockForId(blockEntry.getId()));

    if (inclusion.isIncludeTripStatus()) {
      BlockStatusBean status = getBlockLocationAsStatusBean(blockEntry,
          blockLocation.getServiceDate(), blockLocation);
      details.setStatus(status);
    }

    return details;
  }

  private BlockStatusBean getBlockLocationAsStatusBean(BlockEntry block,
      long serviceDate, BlockLocation blockLocation) {

    BlockStatusBean bean = new BlockStatusBean();

    bean.setStatus("default");
    bean.setServiceDate(serviceDate);
    bean.setTotalDistanceAlongBlock(block.getTotalBlockDistance());

    if (blockLocation != null) {

      bean.setInService(blockLocation.isInService());

      CoordinatePoint location = blockLocation.getLocation();
      bean.setLocation(location);
      
      bean.setScheduledDistanceAlongBlock(blockLocation.getScheduledDistanceAlongBlock());

      TripEntry activeTrip = blockLocation.getActiveTrip();
      if (activeTrip != null) {
        TripBean activeTripBean = _tripBeanService.getTripForId(activeTrip.getId());
        bean.setActiveTrip(activeTripBean);
      }

      StopTimeEntry stop = blockLocation.getClosestStop();
      if (stop != null) {
        StopBean stopBean = _stopBeanService.getStopForId(stop.getStop().getId());
        bean.setClosestStop(stopBean);
        bean.setClosestStopTimeOffset(blockLocation.getClosestStopTimeOffset());
      }
      
      bean.setPredicted(blockLocation.isPredicted());
      bean.setLastUpdateTime(blockLocation.getLastUpdateTime());
      bean.setScheduleDeviation(blockLocation.getScheduleDeviation());
      bean.setDistanceAlongBlock(blockLocation.getDistanceAlongBlock());
      
      AgencyAndId vid = blockLocation.getVehicleId();
      if (vid != null)
        bean.setVehicleId(ApplicationBeanLibrary.getId(vid));

    } else {
      bean.setInService(false);
    }

    return bean;
  }
}
