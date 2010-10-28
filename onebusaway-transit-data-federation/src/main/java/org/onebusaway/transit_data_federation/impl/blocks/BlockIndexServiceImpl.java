package org.onebusaway.transit_data_federation.impl.blocks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockIndexData;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockIndexServiceImpl implements BlockIndexService {

  private Logger _log = LoggerFactory.getLogger(BlockIndexServiceImpl.class);

  private FederatedTransitDataBundle _bundle;

  private TransitGraphDao _graphDao;

  private List<BlockIndex> _blockIndices;

  private Map<String, List<BlockIndex>> _blockIndicesByAgencyId;

  private Map<AgencyAndId, List<BlockIndex>> _blockIndicesByRouteId;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  public void setGraphDao(TransitGraphDao graphDao) {
    _graphDao = graphDao;
  }

  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.BLOCK_INDEX_DATA)
  public void setup() throws Exception {

    File path = _bundle.getBlockIndicesPath();

    if (path.exists()) {

      _log.info("loading block indices data");

      List<BlockIndexData> datas = ObjectSerializationLibrary.readObject(path);

      _blockIndices = new ArrayList<BlockIndex>();
      for (BlockIndexData data : datas)
        _blockIndices.add(data.createIndex(_graphDao));

      _blockIndicesByAgencyId = getBlockIndicesByAgencyId();
      _blockIndicesByRouteId = getBlocksByRouteId();

      _log.info("block indices data loaded");

      clearExistingStopTimeIndices();
      setupBlockStopTimeIndices();

    } else {

      _blockIndices = Collections.emptyList();
      _blockIndicesByAgencyId = Collections.emptyMap();
      _blockIndicesByRouteId = Collections.emptyMap();

      clearExistingStopTimeIndices();
    }
  }

  @Override
  public List<BlockIndex> getBlockIndices() {
    return _blockIndices;
  }

  @Override
  public List<BlockIndex> getBlockIndicesForAgencyId(String agencyId) {
    return list(_blockIndicesByAgencyId.get(agencyId));
  }

  @Override
  public List<BlockIndex> getBlockIndicesForRouteCollectionId(
      AgencyAndId routeCollectionId) {
    return list(_blockIndicesByRouteId.get(routeCollectionId));
  }

  @Override
  public List<BlockStopTimeIndex> getStopTimeIndicesForStop(StopEntry stopEntry) {
    return ((StopEntryImpl) stopEntry).getStopTimeIndices();
  }

  /****
   * Private Methods
   ****/

  /****
   * Private Methods
   ****/

  private List<BlockIndex> list(List<BlockIndex> list) {
    if (list == null)
      return Collections.emptyList();
    return list;
  }

  private Map<String, List<BlockIndex>> getBlockIndicesByAgencyId() {

    Map<String, List<BlockIndex>> blocksByAgencyId = new FactoryMap<String, List<BlockIndex>>(
        new ArrayList<BlockIndex>());

    for (BlockIndex blockIndex : _blockIndices) {
      Set<String> agencyIds = new HashSet<String>();
      for (BlockConfigurationEntry configuration : blockIndex.getBlocks()) {
        for (BlockTripEntry blockTrip : configuration.getTrips())
          agencyIds.add(blockTrip.getTrip().getId().getAgencyId());
      }
      for (String agencyId : agencyIds)
        blocksByAgencyId.get(agencyId).add(blockIndex);
    }
    return blocksByAgencyId;
  }

  private Map<AgencyAndId, List<BlockIndex>> getBlocksByRouteId() {

    Map<AgencyAndId, List<BlockIndex>> blocksByRouteId = new FactoryMap<AgencyAndId, List<BlockIndex>>(
        new ArrayList<BlockIndex>());

    for (BlockIndex blockIndex : _blockIndices) {
      Set<AgencyAndId> routeIds = new HashSet<AgencyAndId>();
      for (BlockConfigurationEntry configuration : blockIndex.getBlocks()) {
        for (BlockTripEntry blockTrip : configuration.getTrips())
          routeIds.add(blockTrip.getTrip().getRouteCollectionId());
      }
      for (AgencyAndId routeId : routeIds)
        blocksByRouteId.get(routeId).add(blockIndex);
    }
    return blocksByRouteId;
  }

  private void setupBlockStopTimeIndices() {

    for (BlockIndex blockIndex : _blockIndices) {

      List<BlockConfigurationEntry> blocks = blockIndex.getBlocks();

      BlockConfigurationEntry firstBlock = blocks.get(0);
      BlockConfigurationEntry lastBlock = blocks.get(blocks.size() - 1);

      List<BlockStopTimeEntry> firstStopTimes = firstBlock.getStopTimes();
      List<BlockStopTimeEntry> lastStopTimes = lastBlock.getStopTimes();

      int n = firstStopTimes.size();

      for (int i = 0; i < n; i++) {

        BlockStopTimeEntry firstStopTime = firstStopTimes.get(i);
        BlockStopTimeEntry lastStopTime = lastStopTimes.get(i);

        ServiceInterval serviceInterval = getStopTimesAsServiceInterval(
            firstStopTime, lastStopTime);

        BlockStopTimeIndex blockStopTimeIndex = new BlockStopTimeIndex(
            blockIndex, i, serviceInterval);

        StopEntryImpl stop = (StopEntryImpl) firstStopTime.getStopTime().getStop();
        stop.addStopTimeIndex(blockStopTimeIndex);
      }
    }

  }

  public void clearExistingStopTimeIndices() {
    // Clear any existing indices
    for (StopEntry stop : _graphDao.getAllStops()) {
      StopEntryImpl stopImpl = (StopEntryImpl) stop;
      stopImpl.getStopTimeIndices().clear();
    }
  }

  private ServiceInterval getStopTimesAsServiceInterval(
      BlockStopTimeEntry firstStopTime, BlockStopTimeEntry lastStopTime) {

    StopTimeEntry st0 = firstStopTime.getStopTime();
    StopTimeEntry st1 = lastStopTime.getStopTime();

    return new ServiceInterval(st0.getArrivalTime(), st0.getDepartureTime(),
        st1.getArrivalTime(), st1.getDepartureTime());
  }
}
