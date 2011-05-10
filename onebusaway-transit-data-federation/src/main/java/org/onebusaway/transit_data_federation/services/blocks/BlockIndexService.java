package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

/**
 * The {@link BlockIndexService}
 * @author bdferris
 *
 */
public interface BlockIndexService {

  public List<BlockTripIndex> getBlockTripIndices();

  public List<BlockTripIndex> getBlockTripIndicesForAgencyId(String agencyId);

  public List<BlockTripIndex> getBlockTripIndicesForRouteCollectionId(
      AgencyAndId routeCollectionId);

  public List<BlockTripIndex> getBlockTripIndicesForBlock(AgencyAndId blockId);

  public List<BlockStopTimeIndex> getStopTimeIndicesForStop(StopEntry stopEntry);

  public List<BlockStopSequenceIndex> getStopSequenceIndicesForStop(
      StopEntry stopEntry);

  public List<BlockSequenceIndex> getAllBlockSequenceIndices();

  /**
   * For the specified stop pair, find all the {@link BlockSequenceIndex} that
   * travel between the two stops. We will actually return a list of
   * {@linkplain Pair pairs} of {@link BlockStopSequenceIndex}, where each block
   * stop sequence index is a pointer into the underlying
   * {@link BlockSequenceIndex} for the two stops.
   * 
   * Effectively, this method tells you all the blocks that travel between the
   * two stops directly.
   * 
   * @param fromStop the source stop
   * @param toStop the destination stop
   * @return the list of all {@link BlockStopSequenceIndex} that link the two
   *         stops directly
   */
  public List<Pair<BlockStopSequenceIndex>> getBlockSequenceIndicesBetweenStops(
      StopEntry fromStop, StopEntry toStop);

  /****
   * Layover Indices
   ****/

  public List<BlockLayoverIndex> getBlockLayoverIndicesForAgencyId(
      String agencyId);

  public List<BlockLayoverIndex> getBlockLayoverIndicesForRouteCollectionId(
      AgencyAndId rotueCollectionId);

  public List<BlockLayoverIndex> getBlockLayoverIndicesForBlock(
      AgencyAndId blockId);

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

  public List<FrequencyStopTripIndex> getFrequencyStopTripIndicesForStop(
      StopEntry stop);

  public List<Pair<FrequencyStopTripIndex>> getFrequencyIndicesBetweenStops(
      StopEntry fromStop, StopEntry toStop);

  /****
   * Block Sequence Indices
   ****/

}
