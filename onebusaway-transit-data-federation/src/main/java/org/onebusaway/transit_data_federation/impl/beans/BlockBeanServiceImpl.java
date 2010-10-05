package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.blocks.BlockBean;
import org.onebusaway.transit_data.model.blocks.BlockConfigurationBean;
import org.onebusaway.transit_data.model.blocks.BlockTripBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.beans.BlockBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockBeanServiceImpl implements BlockBeanService {

  private TransitGraphDao _graph;

  private TripBeanService _tripBeanService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao graph) {
    _graph = graph;
  }

  @Autowired
  public void setTripBeanService(TripBeanService tripBeanService) {
    _tripBeanService = tripBeanService;
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
    return bean;
  }

  /****
   * Private Methods
   ****/

  private BlockConfigurationBean getBlockConfigurationAsBean(
      BlockConfigurationEntry blockConfiguration) {

    BlockConfigurationBean bean = new BlockConfigurationBean();
    List<BlockTripBean> tripBeans = new ArrayList<BlockTripBean>();
    for (BlockTripEntry blockTrip : blockConfiguration.getTrips())
      tripBeans.add(getBlockTripAsBean(blockTrip));
    bean.setTrips(tripBeans);
    return bean;
  }

}
