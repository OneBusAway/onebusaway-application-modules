package org.onebusaway.transit_data_federation.impl.blocks;

import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch.IndexAdapter;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;

public class FrequencyEndTimeIndexAdapter implements
    IndexAdapter<FrequencyBlockStopTimeIndex> {

  public static final FrequencyEndTimeIndexAdapter INSTANCE = new FrequencyEndTimeIndexAdapter();

  @Override
  public double getValue(FrequencyBlockStopTimeIndex source, int index) {
    return source.getEndTimeForIndex(index);
  }
}
