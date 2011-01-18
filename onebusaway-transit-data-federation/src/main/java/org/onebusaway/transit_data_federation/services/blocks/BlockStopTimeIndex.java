package org.onebusaway.transit_data_federation.services.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

public class BlockStopTimeIndex extends AbstractBlockStopTimeIndex {

  public static BlockStopTimeIndex create(BlockTripIndex blockTripIndex,
      int blockSequence) {

    List<BlockTripEntry> tripsList = blockTripIndex.getTrips();
    int n = tripsList.size();

    List<BlockConfigurationEntry> blockConfigs = new ArrayList<BlockConfigurationEntry>(
        n);

    for (BlockTripEntry trip : tripsList)
      blockConfigs.add(trip.getBlockConfiguration());

    int[] stopIndices = new int[n];
    Arrays.fill(stopIndices, blockSequence);

    ServiceInterval serviceInterval = computeServiceInterval(blockTripIndex,
        blockSequence);

    return new BlockStopTimeIndex(blockConfigs, stopIndices, serviceInterval);
  }

  public BlockStopTimeIndex(List<BlockConfigurationEntry> blockConfigs,
      int[] stopIndices, ServiceInterval serviceInterval) {
    super(blockConfigs, stopIndices, serviceInterval);
  }

  public int getArrivalTimeForIndex(int index) {
    BlockConfigurationEntry blockConfig = _blockConfigs.get(index);
    int stopIndex = _stopIndices[index];
    return blockConfig.getArrivalTimeForIndex(stopIndex);
  }

  public int getDepartureTimeForIndex(int index) {
    BlockConfigurationEntry blockConfig = _blockConfigs.get(index);
    int stopIndex = _stopIndices[index];
    return blockConfig.getDepartureTimeForIndex(stopIndex);
  }

  public double getDistanceAlongBlockForIndex(int index) {
    BlockConfigurationEntry blockConfig = _blockConfigs.get(index);
    int stopIndex = _stopIndices[index];
    return blockConfig.getDistanceAlongBlockForIndex(stopIndex);
  }

}
