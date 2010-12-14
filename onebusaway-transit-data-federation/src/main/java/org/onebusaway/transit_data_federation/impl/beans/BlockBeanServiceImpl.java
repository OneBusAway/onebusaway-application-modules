package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data.model.blocks.BlockBean;
import org.onebusaway.transit_data.model.blocks.BlockConfigurationBean;
import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;
import org.onebusaway.transit_data.model.blocks.BlockStopTimeBean;
import org.onebusaway.transit_data.model.blocks.BlockTripBean;
import org.onebusaway.transit_data.model.schedule.FrequencyBean;
import org.onebusaway.transit_data.model.schedule.StopTimeBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.BlockBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopTimeBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockBeanServiceImpl implements BlockBeanService {

  private TransitGraphDao _graph;

  private TripBeanService _tripBeanService;

  private StopTimeBeanService _stopTimeBeanService;

  private BlockCalendarService _blockCalendarService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao graph) {
    _graph = graph;
  }

  @Autowired
  public void setTripBeanService(TripBeanService tripBeanService) {
    _tripBeanService = tripBeanService;
  }

  @Autowired
  public void setStopTimeBeanService(StopTimeBeanService stopTimeBeanService) {
    _stopTimeBeanService = stopTimeBeanService;
  }

  @Autowired
  public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
  }

  @Cacheable
  public BlockBean getBlockForId(AgencyAndId blockId) {

    BlockEntry blockEntry = _graph.getBlockEntryForId(blockId);

    if (blockEntry == null)
      return null;

    BlockBean bean = new BlockBean();

    bean.setId(AgencyAndIdLibrary.convertToString(blockEntry.getId()));

    List<BlockConfigurationBean> configBeans = new ArrayList<BlockConfigurationBean>();
    for (BlockConfigurationEntry blockConfiguration : blockEntry.getConfigurations()) {
      BlockConfigurationBean configBean = getBlockConfigurationAsBean(blockConfiguration);
      configBeans.add(configBean);
    }
    bean.setConfigurations(configBeans);

    return bean;
  }

  @Override
  public BlockTripBean getBlockTripAsBean(BlockTripEntry blockTrip) {

    TripEntry trip = blockTrip.getTrip();
    TripBean tripBean = _tripBeanService.getTripForId(trip.getId());
    if (tripBean == null)
      throw new IllegalStateException("unknown trip: " + trip.getId());

    BlockTripBean bean = new BlockTripBean();
    bean.setTrip(tripBean);
    bean.setAccumulatedSlackTime(blockTrip.getAccumulatedSlackTime());
    bean.setDistanceAlongBlock(blockTrip.getDistanceAlongBlock());

    List<BlockStopTimeBean> blockStopTimes = new ArrayList<BlockStopTimeBean>();
    for (BlockStopTimeEntry blockStopTime : blockTrip.getStopTimes()) {
      BlockStopTimeBean blockStopTimeAsBean = getBlockStopTimeAsBean(blockStopTime);
      blockStopTimes.add(blockStopTimeAsBean);
    }
    bean.setBlockStopTimes(blockStopTimes);

    return bean;
  }

  @Override
  public BlockInstanceBean getBlockInstance(AgencyAndId blockId,
      long serviceDate) {

    BlockInstance blockInstance = _blockCalendarService.getBlockInstance(
        blockId, serviceDate);

    if (blockInstance == null)
      return null;

    return getBlockInstanceAsBean(blockInstance);
  }

  /****
   * Private Methods
   ****/

  private BlockInstanceBean getBlockInstanceAsBean(BlockInstance blockInstance) {

    BlockInstanceBean bean = new BlockInstanceBean();

    BlockConfigurationBean blockConfig = getBlockConfigurationAsBean(blockInstance.getBlock());
    bean.setBlockId(blockConfig.getBlockId());
    bean.setBlockConfiguration(blockConfig);

    long serviceDate = blockInstance.getServiceDate();
    bean.setServiceDate(serviceDate);

    if (blockInstance.getFrequency() != null) {
      FrequencyBean frequency = FrequencyBeanLibrary.getBeanForFrequency(
          serviceDate, blockInstance.getFrequency());
      bean.setFrequency(frequency);
    }

    return bean;
  }

  private BlockConfigurationBean getBlockConfigurationAsBean(
      BlockConfigurationEntry blockConfiguration) {

    BlockConfigurationBean bean = new BlockConfigurationBean();
    ServiceIdActivation serviceIds = blockConfiguration.getServiceIds();

    bean.setBlockId(AgencyAndIdLibrary.convertToString(blockConfiguration.getBlock().getId()));

    List<String> activeServiceIds = new ArrayList<String>();
    for (LocalizedServiceId lsid : serviceIds.getActiveServiceIds())
      activeServiceIds.add(AgencyAndIdLibrary.convertToString(lsid.getId()));
    bean.setActiveServiceIds(activeServiceIds);

    List<String> inactiveServiceIds = new ArrayList<String>();
    for (LocalizedServiceId lsid : serviceIds.getInactiveServiceIds())
      inactiveServiceIds.add(AgencyAndIdLibrary.convertToString(lsid.getId()));
    bean.setInactiveServiceIds(inactiveServiceIds);

    List<BlockTripBean> tripBeans = new ArrayList<BlockTripBean>();
    for (BlockTripEntry blockTrip : blockConfiguration.getTrips())
      tripBeans.add(getBlockTripAsBean(blockTrip));
    bean.setTrips(tripBeans);
    return bean;
  }

  private BlockStopTimeBean getBlockStopTimeAsBean(
      BlockStopTimeEntry blockStopTime) {

    BlockStopTimeBean bean = new BlockStopTimeBean();
    bean.setAccumulatedSlackTime(blockStopTime.getAccumulatedSlackTime());
    bean.setBlockSequence(blockStopTime.getBlockSequence());
    bean.setDistanceAlongBlock(blockStopTime.getDistanceAlongBlock());

    StopTimeBean stopTimeAsBean = _stopTimeBeanService.getStopTimeAsBean(blockStopTime.getStopTime());
    bean.setStopTime(stopTimeAsBean);

    return bean;
  }
}
