package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.blocks.BlockStatusBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data_federation.services.beans.BlockBeanService;
import org.onebusaway.transit_data_federation.services.beans.BlockStatusBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStatusService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockStatusBeanServiceImpl implements BlockStatusBeanService {

  private TripBeanService _tripBeanService;

  private StopBeanService _stopBeanService;

  private BlockStatusService _blockStatusService;

  private BlockBeanService _blockBeanService;

  @Autowired
  public void setTripBeanService(TripBeanService tripBeanService) {
    _tripBeanService = tripBeanService;
  }

  @Autowired
  public void setStopBeanService(StopBeanService stopBeanService) {
    _stopBeanService = stopBeanService;
  }

  @Autowired
  public void setBlockStatusService(BlockStatusService blockStatusService) {
    _blockStatusService = blockStatusService;
  }
  
  @Autowired
  public void setBlockBeanService(BlockBeanService blockBeanService){
    _blockBeanService = blockBeanService;
  }

  /****
   * {@link BlockStatusBeanService} Interface
   ****/

  @Override
  public BlockStatusBean getBlock(AgencyAndId blockId, long serviceDate,
      long time) {
    return bean(_blockStatusService.getBlock(blockId, serviceDate, time));
  }

  @Override
  public BlockStatusBean getBlockForVehicle(AgencyAndId vehicleId, long time) {
    return bean(_blockStatusService.getBlockForVehicle(vehicleId, time));
  }

  @Override
  public ListBean<BlockStatusBean> getBlocksForAgency(String agencyId, long time) {
    return beans(_blockStatusService.getBlocksForAgency(agencyId, time));
  }

  @Override
  public ListBean<BlockStatusBean> getBlocksForRoute(AgencyAndId routeId,
      long time) {
    return beans(_blockStatusService.getBlocksForRoute(routeId, time));
  }

  @Override
  public ListBean<BlockStatusBean> getBlocksForBounds(CoordinateBounds bounds,
      long time) {
    return beans(_blockStatusService.getBlocksForBounds(bounds, time));
  }

  /****
   * Private Methods
   ****/

  private ListBean<BlockStatusBean> beans(List<BlockLocation> locations) {

    List<BlockStatusBean> results = new ArrayList<BlockStatusBean>();

    for (BlockLocation location : locations) {
      BlockStatusBean statusBean = bean(location);
      if (statusBean != null)
        results.add(statusBean);
    }

    return new ListBean<BlockStatusBean>(results, false);
  }

  private BlockStatusBean bean(BlockLocation blockLocation) {

    if (blockLocation == null)
      return null;

    BlockInstance instance = blockLocation.getBlockInstance();
    BlockEntry block = instance.getBlock();
    long serviceDate = instance.getServiceDate();

    BlockStatusBean bean = new BlockStatusBean();
    
    bean.setBlock(_blockBeanService.getBlockForId(block.getId()));

    bean.setStatus("default");
    bean.setServiceDate(serviceDate);
    bean.setTotalDistanceAlongBlock(block.getTotalBlockDistance());

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

    return bean;
  }

}
