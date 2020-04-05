/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data.model.blocks.BlockBean;
import org.onebusaway.transit_data.model.blocks.BlockConfigurationBean;
import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;
import org.onebusaway.transit_data.model.blocks.BlockStopTimeBean;
import org.onebusaway.transit_data.model.blocks.BlockTripBean;
import org.onebusaway.transit_data.model.blocks.ScheduledBlockLocationBean;
import org.onebusaway.transit_data.model.schedule.StopTimeBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.beans.BlockBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopTimeBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocationService;
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

  private ScheduledBlockLocationService _scheduledBlockLocationService;

  private AgencyService _agencyService;

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

  @Autowired
  public void setScheduledBlockLocationService(
      ScheduledBlockLocationService scheduledBlockLocationService) {
    _scheduledBlockLocationService = scheduledBlockLocationService;
  }

  @Autowired
  public void setAgencyService(AgencyService agencyService) {
    _agencyService = agencyService;
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

  @Override
  public ScheduledBlockLocationBean getScheduledBlockLocationFromScheduledTime(
      AgencyAndId blockId, long serviceDate, int scheduledTime) {

    BlockInstance blockInstance = _blockCalendarService.getBlockInstance(
        blockId, serviceDate);

    if (blockInstance == null)
      return null;

    ScheduledBlockLocation blockLocation = _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
        blockInstance.getBlock(), scheduledTime);

    if (blockLocation == null)
      return null;

    return getBlockLocationAsBean(blockLocation);
  }

  @Override
  public BlockInstanceBean getBlockInstanceAsBean(BlockInstance blockInstance) {

    BlockInstanceBean bean = new BlockInstanceBean();

    BlockConfigurationBean blockConfig = getBlockConfigurationAsBean(blockInstance.getBlock());
    bean.setBlockId(blockConfig.getBlockId());
    bean.setBlockConfiguration(blockConfig);

    long serviceDate = blockInstance.getServiceDate();
    bean.setServiceDate(serviceDate);

    return bean;
  }

  /****
   * Private Methods
   ****/

  private ScheduledBlockLocationBean getBlockLocationAsBean(
      ScheduledBlockLocation blockLocation) {

    ScheduledBlockLocationBean bean = new ScheduledBlockLocationBean();

    if (blockLocation.getActiveTrip() != null) {
      BlockTripBean activeTrip = getBlockTripAsBean(blockLocation.getActiveTrip());
      bean.setActiveTrip(activeTrip);
    }

    bean.setDistanceAlongBlock(blockLocation.getDistanceAlongBlock());

    bean.setInService(blockLocation.isInService());
    bean.setLocation(blockLocation.getLocation());
    bean.setScheduledTime(blockLocation.getScheduledTime());
    bean.setStopTimeIndex(blockLocation.getStopTimeIndex());

    return bean;
  }

  private BlockConfigurationBean getBlockConfigurationAsBean(
      BlockConfigurationEntry blockConfiguration) {

    BlockConfigurationBean bean = new BlockConfigurationBean();
    ServiceIdActivation serviceIds = blockConfiguration.getServiceIds();

    AgencyAndId blockId = blockConfiguration.getBlock().getId();
    bean.setBlockId(AgencyAndIdLibrary.convertToString(blockId));

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

    TimeZone tz = _agencyService.getTimeZoneForAgencyId(blockId.getAgencyId());
    bean.setTimeZone(tz.getID());

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
