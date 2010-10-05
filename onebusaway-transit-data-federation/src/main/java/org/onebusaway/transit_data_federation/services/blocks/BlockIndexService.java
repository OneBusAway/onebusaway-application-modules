package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;

public interface BlockIndexService {
  
  public List<BlockIndex> getBlockIndices();
  
  public List<BlockIndex> getBlockIndicesForAgencyId(String agencyId);
  
  public List<BlockIndex> getBlockIndicesForRouteCollectionId(AgencyAndId routeCollectionId);
}
