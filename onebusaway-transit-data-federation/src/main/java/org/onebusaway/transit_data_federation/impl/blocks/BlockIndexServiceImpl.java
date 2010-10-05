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
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockIndexData;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockTripEntry;
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
  public void setup() throws Exception {
    refreshData();
  }

  @Override
  public List<BlockIndex> getBlockIndices() {
    if (_blockIndices == null) {
      synchronized (this) {
        if (_blockIndices == null)
          refreshData();
      }
    }

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

  /****
   * Private Methods
   ****/

  private void refreshData() {

    try {

      _log.info("loading block indices data");

      File path = _bundle.getBlockIndicesPath();

      if (!path.exists())
        return;

      List<BlockIndexData> datas = ObjectSerializationLibrary.readObject(path);

      _blockIndices = new ArrayList<BlockIndex>();
      for (BlockIndexData data : datas)
        _blockIndices.add(data.createIndex(_graphDao));

      _blockIndicesByAgencyId = getBlockIndicesByAgencyId();
      _blockIndicesByRouteId = getBlocksByRouteId();

      _log.info("block indices data loaded");

    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

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
}
