package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public interface BlockIndexService {

  public List<BlockTripIndex> getBlockTripIndices();

  public List<BlockTripIndex> getBlockTripIndicesForAgencyId(String agencyId);

  public List<BlockTripIndex> getBlockTripIndicesForRouteCollectionId(
      AgencyAndId routeCollectionId);
  
  public List<BlockTripIndex> getBlockTripIndicesForBlock(AgencyAndId blockId);

  public List<BlockStopTimeIndex> getStopTimeIndicesForStop(StopEntry stopEntry);

  /****
   * Frequency Indices
   ****/

  public List<FrequencyBlockTripIndex> getFrequencyBlockTripIndices();
  
  public List<FrequencyBlockTripIndex> getFrequencyBlockTripIndicesForAgencyId(
      String agencyId);

  public List<FrequencyBlockTripIndex> getFrequencyBlockTripIndicesForRouteCollectionId(
      AgencyAndId routeCollectionId);
  
  public List<FrequencyBlockTripIndex> getFrequencyBlockTripIndicesForBlock(
      AgencyAndId blockId);

  public List<FrequencyBlockStopTimeIndex> getFrequencyStopTimeIndicesForStop(
      StopEntry stopEntry);
}
