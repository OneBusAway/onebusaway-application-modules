package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public interface BlockIndexService {
  
  public List<BlockIndex> getBlockIndices();
  
  public List<BlockIndex> getBlockIndicesForAgencyId(String agencyId);
  
  public List<BlockIndex> getBlockIndicesForRouteCollectionId(AgencyAndId routeCollectionId);
  
  public List<BlockStopTimeIndex> getStopTimeIndicesForStop(StopEntry stopEntry);
}
