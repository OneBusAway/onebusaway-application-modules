package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockIndexData;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class BlockIndexServiceImpl implements BlockIndexService {

  private FederatedTransitDataBundle _bundle;

  private TransitGraphDao _graphDao;

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

    Map<String, List<BlockIndexData>> dataByAgency = ObjectSerializationLibrary.readObject(_bundle.getBlockIndicesByAgencyPath());
    _blockIndicesByAgencyId = processBlockIndexData(dataByAgency);

    Map<AgencyAndId, List<BlockIndexData>> dataByRoute = ObjectSerializationLibrary.readObject(_bundle.getBlockIndicesByRoutePath());
    _blockIndicesByRouteId = processBlockIndexData(dataByRoute);
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
   * 
   * @return
   ****/

  private <T> Map<T, List<BlockIndex>> processBlockIndexData(
      Map<T, List<BlockIndexData>> dataById) {

    Map<T, List<BlockIndex>> blockIndicesById = new HashMap<T, List<BlockIndex>>();

    for (Map.Entry<T, List<BlockIndexData>> entry : dataById.entrySet()) {
      T id = entry.getKey();
      List<BlockIndexData> datasForId = entry.getValue();
      List<BlockIndex> blockIndices = new ArrayList<BlockIndex>();
      for (BlockIndexData data : datasForId)
        blockIndices.add(data.createIndex(_graphDao));
      blockIndicesById.put(id, blockIndices);
    }

    return blockIndicesById;
  }

  private List<BlockIndex> list(List<BlockIndex> list) {
    if (list == null)
      return Collections.emptyList();
    return list;
  }
}
