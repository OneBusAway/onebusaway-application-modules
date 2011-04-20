package org.onebusaway.transit_data_federation.impl.blocks;

import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch.IndexAdapter;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopSequenceIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.HasIndexedBlockStopTimes;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;

public class IndexAdapters {

  public static final IndexAdapter<HasIndexedBlockStopTimes> BLOCK_STOP_TIME_ARRIVAL_INSTANCE = new BlockStopTimeArrivalTimeIndexAdapter();

  public static final IndexAdapter<HasIndexedBlockStopTimes> BLOCK_STOP_TIME_DEPARTURE_INSTANCE = new BlockStopTimeDepartureTimeIndexAdapter();

  public static final IndexAdapter<BlockStopSequenceIndex> BLOCK_STOP_SEQUENCE_DEPARTURE_INSTANCE = new BlockStopSequenceDepartureTimeIndexAdapter();

  public static final IndexAdapter<BlockConfigurationEntry> BLOCK_CONFIG_DEPARTURE_INSTANCE = new BlockConfigDepartureTimeIndexAdapter();

  public static final IndexAdapter<BlockConfigurationEntry> BLOCK_CONFIG_DISTANCE_INSTANCE = new BlockConfigDistanceAlongBlockIndexAdapter();

  public static final IndexAdapter<FrequencyBlockStopTimeIndex> FREQUENCY_END_TIME_INSTANCE = new FrequencyEndTimeIndexAdapter();

  public static final IndexAdapter<FrequencyBlockStopTimeIndex> FREQUENCY_START_TIME_INSTANCE = new FrequencyStartTimeIndexAdapter();

  /****
   * 
   ****/

  private static class BlockStopTimeArrivalTimeIndexAdapter implements
      IndexAdapter<HasIndexedBlockStopTimes> {

    @Override
    public double getValue(HasIndexedBlockStopTimes source, int index) {
      return source.getArrivalTimeForIndex(index);
    }
  }

  private static class BlockStopTimeDepartureTimeIndexAdapter implements
      IndexAdapter<HasIndexedBlockStopTimes> {

    @Override
    public double getValue(HasIndexedBlockStopTimes source, int index) {
      return source.getDepartureTimeForIndex(index);
    }
  }

  private static class BlockStopSequenceDepartureTimeIndexAdapter implements
      IndexAdapter<BlockStopSequenceIndex> {

    @Override
    public double getValue(BlockStopSequenceIndex source, int index) {
      return source.getDepartureTimeForIndex(index);
    }
  }

  private static class BlockConfigDepartureTimeIndexAdapter implements
      IndexAdapter<BlockConfigurationEntry> {

    @Override
    public double getValue(BlockConfigurationEntry source, int index) {
      return source.getDepartureTimeForIndex(index);
    }
  }

  private static class BlockConfigDistanceAlongBlockIndexAdapter implements
      IndexAdapter<BlockConfigurationEntry> {

    @Override
    public double getValue(BlockConfigurationEntry source, int index) {
      return source.getDistanceAlongBlockForIndex(index);
    }
  }

  private static class FrequencyEndTimeIndexAdapter implements
      IndexAdapter<FrequencyBlockStopTimeIndex> {

    @Override
    public double getValue(FrequencyBlockStopTimeIndex source, int index) {
      return source.getEndTimeForIndex(index);
    }
  }

  private static class FrequencyStartTimeIndexAdapter implements
      IndexAdapter<FrequencyBlockStopTimeIndex> {

    @Override
    public double getValue(FrequencyBlockStopTimeIndex source, int index) {
      return source.getStartTimeForIndex(index);
    }
  }
}
