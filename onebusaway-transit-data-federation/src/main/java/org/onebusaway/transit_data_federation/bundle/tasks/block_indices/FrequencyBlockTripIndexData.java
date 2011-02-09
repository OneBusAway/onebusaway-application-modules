package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;

public class FrequencyBlockTripIndexData implements Serializable {

  private static final long serialVersionUID = 1L;

  private final List<BlockTripReference> _trips;

  private final List<FrequencyEntry> _frequencies;

  private final FrequencyServiceIntervalBlock _serviceIntervalBlock;

  public FrequencyBlockTripIndexData(List<BlockTripReference> trips,
      List<FrequencyEntry> frequencies,
      FrequencyServiceIntervalBlock serviceIntervalBlock) {
    _trips = trips;
    _frequencies = frequencies;
    _serviceIntervalBlock = serviceIntervalBlock;
  }

  public List<BlockTripReference> getTrips() {
    return _trips;
  }

  public List<FrequencyEntry> getFrequencies() {
    return _frequencies;
  }

  public FrequencyServiceIntervalBlock getServiceIntervalBlock() {
    return _serviceIntervalBlock;
  }

  public FrequencyBlockTripIndex createIndex(TransitGraphDao dao) {

    ArrayList<BlockTripEntry> trips = new ArrayList<BlockTripEntry>();

    for (int i = 0; i < _trips.size(); i++) {

      BlockTripReference tripReference = _trips.get(i);
      BlockTripEntry blockTrip = ReferencesLibrary.getReferenceAsTrip(tripReference, dao);
      trips.add(blockTrip);
    }

    trips.trimToSize();

    return new FrequencyBlockTripIndex(trips, _frequencies,
        _serviceIntervalBlock);
  }
}
