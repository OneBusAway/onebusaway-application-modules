package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

/**
 * A FrequencyBlockIndex is a collection of {@link BlockConfigurationEntry}
 * elements that have the following properties in common:
 * 
 * 1) Each {@link BlockConfigurationEntry} refers to the same stop sequence
 * pattern and underlying shape of travel.
 * 
 * 2) Each {@link BlockConfigurationEntry} has the same set of service ids (see
 * {@link BlockConfigurationEntry#getServiceIds()}
 * 
 * 3) The list of {@link BlockConfigurationEntry} elements is sorted by arrival
 * time and no block ever overtakes another block.
 * 
 * 4) The {@link ServiceIntervalBlock} additionally captures the min and max
 * arrival and departure times for each block in the list, in the same sorted
 * order as the block list.
 * 
 * These assumptions allow us to do efficient searches for blocks that are
 * active at a particular time.
 * 
 * @author bdferris
 * @see BlockCalendarService
 */
public class FrequencyBlockTripIndex extends AbstractBlockTripIndex {

  private final List<FrequencyEntry> _frequencies;

  private final FrequencyServiceIntervalBlock _serviceIntervalBlock;

  /**
   * See the requirements in the class documentation.
   * 
   * @param trips
   * @param serviceIdIntervals
   * @param serviceIntervalBlock
   */
  public FrequencyBlockTripIndex(List<BlockTripEntry> trips,
      List<FrequencyEntry> frequencies,
      FrequencyServiceIntervalBlock serviceIntervalBlock) {
    super(trips);
    if (frequencies == null)
      throw new IllegalArgumentException("frequencies is null");
    if (frequencies.isEmpty())
      throw new IllegalArgumentException("frequencies is empty");

    _frequencies = frequencies;
    _serviceIntervalBlock = serviceIntervalBlock;
  }

  public List<FrequencyEntry> getFrequencies() {
    return _frequencies;
  }

  public FrequencyServiceIntervalBlock getServiceIntervalBlock() {
    return _serviceIntervalBlock;
  }

  @Override
  public String toString() {
    return "FrequencyBlockTripIndex [trips=" + _trips
        + ", serviceIntervalBlock=" + _serviceIntervalBlock + "]";
  }
}
